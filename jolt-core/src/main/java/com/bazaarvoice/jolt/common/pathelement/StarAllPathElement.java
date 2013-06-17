package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.common.WalkedPath;

import java.util.Collections;

/**
 * PathElement for the lone "*" wildcard.   In this case we can avoid doing any
 *  regex or string comparison work at all.
 */
public class StarAllPathElement implements StarPathElement {

    public StarAllPathElement( String key ) {
        if ( ! "*".equals( key ) ) {
            throw new IllegalArgumentException( "StarAllPathElement key should just be a single '*'" );
        }
    }

    /**
     * @param literal test to see if the provided string will match this Element's regex
     * @return true if the provided literal will match this Element's regex
     */
    @Override
    public boolean stringMatch( String literal ) {
        return true;
    }

    @Override
    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        return new LiteralPathElement(dataKey, Collections.<String>emptyList() );
    }

    @Override
    public String getCanonicalForm() {
        return "*";
    }

    @Override
    public String getRawKey() {
        return "*";
    }
}
