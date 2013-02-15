package com.bazaarvoice.jolt.shiftr.pathelement;

import com.bazaarvoice.jolt.shiftr.WalkedPath;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * PathElement for the a single "*" wildcard.   In this case we can avoid doing any
 *  regex work by doing String begins and ends with comparisons.
 */
public class StarSinglePathElement extends BasePathElement implements StarPathElement {

    private final String prefix,suffix;

    public StarSinglePathElement( String key ) {
        super(key);

        if ( StringUtils.countMatches( key, "*" ) != 1 ) {
            throw new IllegalArgumentException( "StarSinglePathElement should only have one '*' in its key." );
        }
        else if ( "*".equals( key ) ) {
            throw new IllegalArgumentException( "StarSinglePathElement should have a key that is just '*'." );
        }

        if ( key.startsWith( "*" ) ) {
            prefix = "";
            suffix = key.substring( 1 );
        }
        else if ( key.endsWith( "*" ) ) {
            prefix = key.substring( 0, key.length() -1 );
            suffix = "";
        }
        else
        {
            String[] split = key.split( "\\*" );
            prefix = split[0];
            suffix = split[1];
        }
    }

    /**
     * @param literal test to see if the provided string will match this Element's regex
     * @return true if the provided literal will match this Element's regex
     */
    @Override
    public boolean stringMatch( String literal ) {
        return literal.startsWith( prefix ) && literal.endsWith( suffix )  // the ends match
                && literal.length() > prefix.length() + suffix.length();   // and the * captures something
    }

    @Override
    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {

        if ( stringMatch( dataKey ) )  {
            List<String> subKeys = new ArrayList<String>(1);

            String starPart = dataKey.substring( prefix.length(), dataKey.length() - suffix.length() );
            subKeys.add( starPart );

            return new LiteralPathElement(dataKey, subKeys);
        }

        return null;
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }
}
