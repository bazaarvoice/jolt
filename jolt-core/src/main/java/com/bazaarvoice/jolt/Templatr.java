/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.templatr.OpMode;
import com.bazaarvoice.jolt.templatr.spec.TemplatrCompositeSpec;
import com.bazaarvoice.jolt.templatr.TemplatrSpecBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Base Templatr transform that to behave differently based on provided opMode
 */
public abstract class Templatr implements SpecDriven, ContextualTransform {

    private final TemplatrCompositeSpec rootSpec;

    @SuppressWarnings( "unchecked" )
    private Templatr(Object spec, OpMode opMode) {
        if ( spec == null ){
            throw new SpecException( opMode.name() + " expected a spec of Map type, got 'null'." );
        }
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( opMode.name() + " expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }

        TemplatrSpecBuilder templatrSpecBuilder = new TemplatrSpecBuilder( opMode );
        rootSpec = new TemplatrCompositeSpec( ROOT_KEY, (Map<String, Object>) spec, opMode, templatrSpecBuilder );
    }

    @Override
    public Object transform( final Object input, final Map<String, Object> context ) {

        Map<String, Object> contextWrapper = new HashMap<>(  );
        contextWrapper.put( ROOT_KEY, context );

        MatchedElement rootLpe = new MatchedElement( ROOT_KEY );
        WalkedPath walkedPath = new WalkedPath();
        walkedPath.add( input, rootLpe );

        rootSpec.apply( ROOT_KEY, input, walkedPath, null, contextWrapper );
        return input;
    }

    /**
     * This variant of templatr creates the key/index is missing,
     * and overwrites the value if present
     */
    public static final class Overwritr extends Templatr {

        public Overwritr( Object spec ) {
            super(spec, OpMode.OVERWRITR );
        }
    }

    /**
     * This variant of templatr only writes when the key/index is missing
     */
    public static final class Definr extends Templatr {

        public Definr( final Object spec ) {
            super( spec, OpMode.DEFINER );
        }
    }

    /**
     * This variant of templatr only writes when the key/index is present and the value is null
     */
    public static class Defaultr extends Templatr {

        public Defaultr( final Object spec ) {
            super( spec, OpMode.DEFAULTR );
        }
    }
}
