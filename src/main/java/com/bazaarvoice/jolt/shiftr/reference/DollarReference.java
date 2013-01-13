package com.bazaarvoice.jolt.shiftr.reference;

public class DollarReference extends BaseReference {

    public DollarReference( String refStr ) {
        super(refStr);
    }

    @Override
    protected char getToken() {
        return '$';
    }
}