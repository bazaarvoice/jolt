package com.bazaarvoice.jolt.shiftr.pathelement;

import org.apache.commons.lang.StringUtils;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.WalkedPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PathElement {

    // Todo move this somewhere else.  This is the reason this class is still in the shiftr package
    //  as it is doing shiftr specific checks.
    // PathElement and its subclasses could be pulled out to a common, once all the shiftr specific
    //  logic is extracted.
    public static List<PathElement> parse( String key )  {

        if ( key.contains("@") ) {
            return Arrays.<PathElement>asList( new AtPathElement( key ) );
        }
        else if ( key.contains("$") ) {
            return Arrays.<PathElement>asList( new DollarPathElement( key ) );
        }
        else if ( key.contains("[") ) {

            if ( StringUtils.countMatches( key, "[" ) != 1 || StringUtils.countMatches( key, "]" ) != 1 ) {
                throw new SpecException( "Invalid key:" + key + " has too many [] references.");
            }

            // is canonical array?
            if ( key.charAt( 0 ) == '[' && key.charAt( key.length() - 1 ) == ']') {
                return Arrays.<PathElement>asList( new ArrayPathElement( key ) );
            }

            // Split syntactic sugar of "photos[]" --> [ "photos", "[]" ]
            //  or                      "bob-&(3,1)-smith[&0]" --> [ "bob-&(3,1)-smith", "[&(0,0)]" ]

            String canonicalKey = key.replace( "[", ".[" );
            String[] subkeys = canonicalKey.split( "\\." );

            List<PathElement> subElements = parse(  subkeys ); // at this point each sub key should be a valid key, so just recall parse

            for ( int index = 0; index < subElements.size() - 1; index++ ) {
                PathElement v = subElements.get( index );
                if ( v instanceof ArrayPathElement ) {
                    throw new SpecException( "Array [..] must be the last thing in the key, was:" + key );
                }
            }

            return subElements;
        }
        else if ( key.contains("&") ) {
            if ( key.contains("*") )
            {
                throw new SpecException("Can't mix * with & ) ");
            }
            return Arrays.<PathElement>asList( new AmpPathElement( key ) );
        }
        else if ( key.contains("*") ) {
            return Arrays.<PathElement>asList( new StarPathElement( key ) );
        }
        else {
            return Arrays.<PathElement>asList( new LiteralPathElement( key ) );
        }
    }

    public static List<PathElement> parse( String[] keys ) {
        ArrayList<PathElement> paths = new ArrayList<PathElement>();

        for( String key: keys ) {
            List<PathElement> subPaths = parse( key );
            for ( PathElement path : subPaths ) {
                paths.add( path );
            }
        }

        paths.trimToSize();
        return paths;
    }

    private String rawKey;

    public PathElement( String key ) {
        this.rawKey = key;
    }

    public String getRawKey() {
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
     * @param walkedPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing its match
     * @return null or a matched LiteralPathElement
     */
    public abstract LiteralPathElement match( String dataKey, WalkedPath walkedPath );

    /**
     * Evaluate this key as if it is an write path element.
     * @param walkedPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing
     * @return String path element to use for write tree building
     */
    public abstract String evaluate( WalkedPath walkedPath );

    /**
     * Get the canonical form of this PathElement.  Really only interesting for the Reference Path element, where
     *  it will expand "&" to "&0(0)".
     * @return canonical String version of this PathElement
     */
    public abstract String getCanonicalForm();


}
