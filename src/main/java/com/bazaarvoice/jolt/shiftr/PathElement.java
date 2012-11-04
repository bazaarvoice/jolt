package com.bazaarvoice.jolt.shiftr;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class PathElement {

    public static PathElement parse( String key )  {
        if ( key.contains("&") || key.contains("[") ) {
            if ( key.contains("*") )
            {
                throw new IllegalArgumentException("Can't mix * and ( & or [] ) ");
            }
            return new ReferencePathElement( key );
        }
        if ( key.contains("*") ) {
            return new StarPathElement( key );
        }
        if ( key.contains("@") ) {
            return new AtPathElement( key );
        }

        return new LiteralPathElement( key );
    }

    public static List<PathElement> parse( String[] keys ) {
        ArrayList<PathElement> paths = new ArrayList<PathElement>();

        for( String key: keys ) {
            paths.add( parse( key ) );
        }
        return paths;
    }

    private String rawKey;

    public PathElement( String key ) {
        this.rawKey = key;
    }

    protected String getRawKey() {
        return rawKey;
    }

    public String toString() {
        return getCanonicalForm();
    }

    /**
     * See if this PathElement matches the given dataKey.  If it does not match, this method returns null.
     *
     * If this PathElement does match, it returns a LiteralPathElement with subKeys filled in.
     *
     * @param dataKey String key value from the input data
     * @param specInputPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing its match
     * @return null or a matched LiteralPathElement
     */
    public abstract LiteralPathElement match( String dataKey, Path<LiteralPathElement> specInputPath );

    /**
     * Evaluate this key as if it is an output path element.
     * @param specInputPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing
     * @return String path element to use for output tree building
     */
    public abstract String evaluate( Path<LiteralPathElement> specInputPath );

    /**
     * Get the canonical form of this PathElement.  Really only interesting for the Reference Path element, where
     *  it will expand "&" to "&0(0)".
     * @return
     */
    public abstract String getCanonicalForm();




    public static class LiteralPathElement extends PathElement {

        private List<String> subKeys = new ArrayList<String>();

        public LiteralPathElement( String key ) {
            super(key);
            subKeys.add( key ); // always add the full key to index 0
        }

        public LiteralPathElement( String key, List<String> subKeys ) {
            super(key);
            this.subKeys.add( key );
            this.subKeys.addAll( subKeys );
        }

        @Override
        public String evaluate( Path<LiteralPathElement> specInputPath ) {
            return getRawKey();
        }

        @Override
        public LiteralPathElement match( String dataKey, Path<LiteralPathElement> specInputPath ) {
            return getRawKey().equals( dataKey ) ? this : null ;
        }

        @Override
        public String getCanonicalForm() {
            return getRawKey();
        }

        public String getSubKeyRef( int index ) {
            if ((index < 0) || (index >= this.subKeys.size())) {
                throw new IndexOutOfBoundsException( "LiteralPathElement "+ StringUtils.join( this.subKeys.toArray() ) +" cannot be indexed with index "+index );
            }
            return subKeys.get( index );
        }

        public int getSubKeyCount(){
            return subKeys.size();
        }
    }

    public static class AtPathElement extends PathElement {
        public AtPathElement( String key ) {
            super(key);

            if ( ! "@".equals( key ) ) {
                throw new IllegalArgumentException( "'References Input' key '@', can only be a single '@'.  Offending key : " + key );
            }
        }

        public String evaluate( Path<LiteralPathElement> specInputPath ) {
            throw new UnsupportedOperationException("Don't call evaluate on the '@'");
        }

        public LiteralPathElement match( String dataKey, Path<LiteralPathElement> specInputPath ) {
            return specInputPath.lastElement();  // copy what our parent was so that output keys of &0 and &1 both work.
        }

        @Override
        public String getCanonicalForm() {
            return "@";
        }
    }

    /**
     * Non-greed * based Path Element.
     */
    public static class StarPathElement extends PathElement {

        private Pattern pattern;

        public StarPathElement( String key ) {
            super(key);

            // "rating-*-*"  ->  "^rating-(.*?)-(.*?)$"
            String regex = "^" + key.replace("*", "(.*?)")  + "$";

            pattern = Pattern.compile( regex );
        }

        public String evaluate( Path<LiteralPathElement> specInputPath ) {
            throw new UnsupportedOperationException("Don't call evaluate on the '*'");
        }

        public LiteralPathElement match( String dataKey, Path<LiteralPathElement> specInputPath ) {
            Matcher matcher = pattern.matcher( dataKey );
            if ( ! matcher.find() ) {
                return null;
            }

            List<String> subKeys = new ArrayList<String>();
            int groupCount = matcher.groupCount();
            for ( int index = 1; index <= groupCount; index++) {
                subKeys.add( matcher.group( index ) );
            }

            return new LiteralPathElement(dataKey, subKeys);
        }

        @Override
        public String getCanonicalForm() {
            return getRawKey();
        }
    }

    /**
     * PathElement class that handles keys with & and [] values, like input: "photos-&1(1)" and output : "photos[&1(1)]"
     * It breaks down the string into a series of String or Reference tokens, that can be used to
     * 1) match input like "photos-5" where "&1(1)" evaluated to 5
     * 2) compute an output stringKey like "photos[5]"
     */
    public static class ReferencePathElement extends PathElement {

        protected List tokens = new ArrayList();
        protected String canonicalForm;

        public ReferencePathElement( String key ) {
            super(key);

            StringBuilder literal = new StringBuilder();
            StringBuilder canonicalBuilder = new StringBuilder();

            int numArrayTokens = 0;
            int index = 0;
            while( index < key.length() ) {

                char c = key.charAt( index );

                // beginning of reference
                if ( c == '&' || c == '[' ) {

                    // store off any literal text captured thus far
                    if ( literal.length() > 0 ) {
                        tokens.add( literal.toString() );
                        canonicalBuilder.append( literal );
                        literal = new StringBuilder();
                    }
                    int refEnd = 0;
                    Reference ref = null;

                    if ( c == '[' ) {
                        refEnd = findEndOfArrayReference( key.substring( index ) );  // look ahead and find the closing ']'
                        ref = Reference.newReference(true, key.substring(index + 1, index + refEnd) ); // chomp off the leading and trailing [ ]
                        numArrayTokens++;

                        canonicalBuilder.append( "[" );
                        canonicalBuilder.append( "&(" ).append( ref.pathIndex );
                        canonicalBuilder.append( "," ).append( ref.keyGroup ).append( ")" );
                        canonicalBuilder.append( "]" );
                    }
                    else {
                        refEnd = findEndOfReference( key.substring( index + 1 ) );
                        ref = Reference.newReference(false, key.substring(index, index + refEnd + 1) );
                        canonicalBuilder.append( "&(" ).append( ref.pathIndex );
                        canonicalBuilder.append( "," ).append( ref.keyGroup ).append( ")" );
                    }
                    tokens.add( ref );
                    index += refEnd;
                }
                else {
                    literal.append( c );
                }
                index++;
            }
            if ( literal.length() > 0 ) {
                tokens.add( literal.toString() );
            }

            this.canonicalForm = canonicalBuilder.toString();

            // Checks
            if ( numArrayTokens > 1 ) {
                throw new IllegalArgumentException( "Key " + key + " can only contain one array reference." );
            }
            if ( numArrayTokens == 1 ) {
                Object lastToken = tokens.get( tokens.size() -1 );
                if (lastToken instanceof String ) {
                    throw new IllegalArgumentException( "Error in Key " + key + " : Array Reference has to be the last component of the key." );
                }
                Reference ref = (Reference) lastToken;
                if ( ! ref.isArray ) {
                    throw new IllegalArgumentException( "Error in Key " + key + " : Array Reference has to be the last component of the key." );
                }
            }
        }

        private static int findEndOfArrayReference( String key ) {
            int endOfArray = key.indexOf( ']' );
            if ( endOfArray <= 0 ) {
                throw new IllegalArgumentException( "Invalid Key array reference of '" + key + "'.  Missing ']'." );
            }
            return endOfArray;
        }

        private static int findEndOfReference( String key ) {
            if( "".equals( key ) ) {
                return 0;
            }

            for( int index = 0; index < key.length(); index++ ){
                char c = key.charAt( index );
                // keep going till we see something other than a digit, parens, or comma
                if( ! Character.isDigit( c ) && c != '(' && c != ')' && c != ',') {
                    return index;
                }
            }
            return key.length();
        }

        @Override
        public String getCanonicalForm() {
            return canonicalForm;
        }

        @Override
        public String evaluate( Path<LiteralPathElement> specInputPath ) {

            // Walk thru our tokens and build up a string
            // Use the supplied Path to fill in our token References
            StringBuffer output = new StringBuffer();

            for ( Object token : tokens ) {
                if ( token instanceof String ) {
                    output.append( token );
                }
                else {
                    Reference ref = (Reference) token;

                    if ( ref.isArray ) {
                        if ( ref.arrayIndex != -1 ) {
                            output.append( "[" + ref.arrayIndex + "]");
                        }
                        else {
                            LiteralPathElement pe = specInputPath.elementFromEnd( ref.pathIndex );
                            String keyPart = pe.getSubKeyRef( ref.keyGroup );
                            int index = Integer.parseInt( keyPart );
                            output.append( "[" + index + "]");
                        }
                    }
                    else {
                        LiteralPathElement pe = specInputPath.elementFromEnd( ref.pathIndex );
                        String keyPart = pe.getSubKeyRef( ref.keyGroup );
                        output.append( keyPart );
                    }
                }
            }

            return output.toString();
        }

        @Override
        public LiteralPathElement match( String dataKey, Path<LiteralPathElement> specInputPath ) {
            String evaled = evaluate( specInputPath );
            if ( evaled.equals( dataKey ) ) {
                return new LiteralPathElement( evaled );
            }
            return null;
        }
    }
}
