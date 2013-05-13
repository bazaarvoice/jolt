package com.bazaarvoice.jolt.shiftr.reference;

/**
 * This class parses the Jolt & syntax into useful programmatic constructs.
 *
 * Valid Syntax is :  &   &1   &(1)   &(1,1)
 */
public class AmpReference extends BasePathAndGroupReference {

    public static final Character TOKEN = '&';

    public AmpReference( String refStr ) {
        super(refStr);
    }

    @Override
    protected char getToken() {
        return TOKEN;
    }
}