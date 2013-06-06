package com.bazaarvoice.jolt.chainr;

import com.bazaarvoice.jolt.Transform;
import com.bazaarvoice.jolt.exception.TransformException;

public class ExplodingTestTransform implements Transform {

    @Override
    public Object transform( Object input ) {
        throw new TransformException( "kaboom" );
    }
}
