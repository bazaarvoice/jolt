package com.bazaarvoice.jolt.shiftr.pathelement;

public abstract class BasePathElement implements PathElement {

    private final String rawKey;

    public BasePathElement( String key ) {
        rawKey = key;
    }

    @Override
    public String getRawKey() {
        return rawKey;
    }

    public String toString() {
        return getCanonicalForm();
    }
}
