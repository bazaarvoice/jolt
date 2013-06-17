package com.bazaarvoice.jolt.common.reference;

/**
 * TODO : Refactor the out to it's own class, as it really isn't a "Reference"
 * This is just a cheap hack at the moment.
 */
public class HashReference extends BasePathReference {

    public static final Character TOKEN = '#';

    public HashReference( String refStr ) {
        super(refStr);
    }

    @Override
    protected char getToken() {
        return TOKEN;
    }
}