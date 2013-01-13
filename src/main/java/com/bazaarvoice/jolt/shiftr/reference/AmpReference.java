package com.bazaarvoice.jolt.shiftr.reference;

import java.util.regex.Pattern;

/**
 * This class parses the Jolt & syntax into useful programmatic constructs.
 *
 * Valid Syntax is :  &   &1   &(1)   &(1,1)
 */
public class AmpReference extends BaseReference {

    public AmpReference( String refStr ) {
        super(refStr);
    }

    @Override
    protected char getToken() {
        return '&';
    }
}