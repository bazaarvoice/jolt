package com.bazaarvoice.jolt.chainr;

import com.bazaarvoice.jolt.SpecTransform;

/**
 * Chainr should barf on this class, as it is a SpecTransform without a single arg constructor.
 * This class is reference from a JSON test fixture.
 */
public class BadSpecTransform implements SpecTransform {

    @Override
    public Object transform( Object input ) {
        return input;
    }
}

