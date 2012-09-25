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

    public static List<PathElement> parseDotNotation( String dotNotation ) {

        if ( dotNotation.contains("@") || dotNotation.contains("*") ) {
            throw new IllegalArgumentException("DotNotation parse (output key) can not contain '@' or '*'.");
        }

        if ( ( dotNotation == null ) || ( "".equals( dotNotation ) ) ) {   // TODO blank?
            return new ArrayList<PathElement>();
        } else {
            String[] split = dotNotation.split( "\\." );
            return PathElement.parse( split );
        }
    }

    protected String rawKey;

    public PathElement( String key ) {
        this.rawKey = key;
    }

    public String toString() {
        return rawKey;
    }

    /**
     * See if this PathElement matches the given input dataKey.  If it does not match, this method returns null.
     *
     * If this PathElement does match, it returns a LiteralPathElement with subKeys filled in.
     *
     * @param dataKey String key value from the input data
     * @param specInputPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing its match
     * @return null or a matched LiteralPathElement
     */
    public abstract LiteralPathElement matchInput( String dataKey, Path<LiteralPathElement> specInputPath );

    /**
     * Evaluate this key as if it is an output path element.
     * @param specInputPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing
     * @return String path element to use for output tree building
     */
    public abstract String evaluateAsOutputKey( Path<LiteralPathElement> specInputPath );


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

        public String evaluateAsOutputKey( Path<LiteralPathElement> specInputPath ) {
            return rawKey;
        }

        public LiteralPathElement matchInput( String dataKey, Path<LiteralPathElement> specInputPath ) {
            return rawKey.equals( dataKey ) ? this : null ;
        }

        public String getSubKeyRef( int index ) {
            return subKeys.get( index );
        }

        public int getSubKeyCount(){
            return subKeys.size();
        }
    }

    public static class OrPathElement extends PathElement {

        List<PathElement> elements;
        public OrPathElement( String key ) {
            super(key);

            String[] split = key.split( "|" );
            elements = PathElement.parse( split );
        }

        public String evaluateAsOutputKey( Path<LiteralPathElement> specInputPath ) {
            throw new UnsupportedOperationException("Don't call evaluateAsOutputKey on the '|'");
        }

        public LiteralPathElement matchInput( String dataKey, Path<LiteralPathElement> specInputPath ) {
            for ( PathElement pe : elements ) {
                LiteralPathElement pathElement = pe.matchInput( dataKey, specInputPath );
                if ( pathElement != null ) {
                    return pathElement;
                }
            }
            return null;
        }
    }

    public static class AtPathElement extends PathElement {
        public AtPathElement( String key ) {
            super(key);
        }

        public String evaluateAsOutputKey( Path<LiteralPathElement> specInputPath ) {
            throw new UnsupportedOperationException("Don't call evaluateAsOutputKey on the '@'");
        }

        public LiteralPathElement matchInput( String dataKey, Path<LiteralPathElement> specInputPath ) {
            return new LiteralPathElement( dataKey );
        }
    }

    public static class StarPathElement extends PathElement {

        private Pattern pattern;
        private int numStars = 0;

        public StarPathElement( String key ) {
            super(key);

            numStars = StringUtils.countMatches( key, "*" );

            String regex = "^" + key.replace("*", "(.*?)")  + "$";

            // "rating-*-*"  ->  "^rating-(.*?)-(.*?)$"
            pattern = Pattern.compile( regex );
        }

        public String evaluateAsOutputKey( Path<LiteralPathElement> specInputPath ) {
            throw new UnsupportedOperationException("Don't call evaluateAsOutputKey on the '*'");
        }

        public LiteralPathElement matchInput( String dataKey, Path<LiteralPathElement> specInputPath ) {
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
    }

    public static class ReferencePathElement extends PathElement {

        List tokens = new ArrayList();

        public ReferencePathElement( String key ) {
            super(key);

            StringBuffer literal = new StringBuffer();

            int index = 0;
            while( index < key.length() ) {

                char c = key.charAt( index );

                // beginning of reference
                if ( c == '&' || c == '[' ) {

                    // store off any literal text captured thus far
                    if ( literal.length() > 0 ) {
                        tokens.add( literal.toString() );
                        literal = new StringBuffer();
                    }
                    int subEnd = 0;
                    Reference ref = null;

                    if ( c == '[' ) {
                        subEnd = findEndOfArrayReference( key.substring( index ) );
                        ref = Reference.newReference(true, key.substring(index + 1, index + subEnd) ); // chomp off the leading and trailing [ ]
                    }
                    else {
                        subEnd = findEndOfReference( key.substring( index + 1 ) );
                        ref = Reference.newReference(false, key.substring(index, index + subEnd + 1) );
                    }
                    tokens.add( ref );
                    index += subEnd;
                }
                else {
                    literal.append( c );
                }
                index++;
            }
            if ( literal.length() > 0 ) {
                tokens.add( literal.toString() );
            }
        }

        private static int findEndOfArrayReference( String key ) {
            int endOfArray = key.indexOf( ']' );
            if ( endOfArray <= 0 ) {
                throw new IllegalArgumentException( "invalid array reference of " + key + "' " );
            }
            return endOfArray;
        }

        private static int findEndOfReference( String key ) {
            if( "".equals( key ) ) {
                return 0;
            }

            for( int index = 0; index < key.length(); index++ ){
                char c = key.charAt( index );
                if( ! Character.isDigit( c ) && c != '(' && c != ')' ) {
                    return index;
                }
            }
            return key.length();
        }

        public String evaluateAsOutputKey( Path<LiteralPathElement> specInputPath ) {

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
        public LiteralPathElement matchInput( String dataKey, Path<LiteralPathElement> specInputPath ) {
            String evaled = evaluateAsOutputKey( specInputPath );
            if ( evaled.equals( dataKey ) ) {
                return new LiteralPathElement( evaled );
            }
            return null;
        }
    }
}
