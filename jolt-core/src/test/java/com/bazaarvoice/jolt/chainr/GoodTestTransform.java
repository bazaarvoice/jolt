package com.bazaarvoice.jolt.chainr;

import com.bazaarvoice.jolt.SpecTransform;

public class GoodTestTransform implements SpecTransform {

    private Object spec;

    public GoodTestTransform( Object spec ) {
        this.spec = spec;
    }

    @Override
    public Object transform( Object input ) {
        return new DelegationResult( input, spec );
    }
}
