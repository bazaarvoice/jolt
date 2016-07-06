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
import com.bazaarvoice.jolt.templatr.TemplatrSpecBuilder;
import com.bazaarvoice.jolt.templatr.function.Function;
import com.bazaarvoice.jolt.templatr.function.Math;
import com.bazaarvoice.jolt.templatr.function.Strings;
import com.bazaarvoice.jolt.templatr.spec.TemplatrCompositeSpec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Templatr transform that to behave differently based on provided opMode
 */
public abstract class Templatr implements SpecDriven, ContextualTransform {

    private static final Map<String, Function> STOCK_FUNCTIONS = new HashMap<>(  );

    static {
        STOCK_FUNCTIONS.put( "toLower", new Strings.toLowerCase() );
        STOCK_FUNCTIONS.put( "toUpper", new Strings.toUpperCase() );
        STOCK_FUNCTIONS.put( "concat", new Strings.concat() );

        STOCK_FUNCTIONS.put( "minOf", new com.bazaarvoice.jolt.templatr.function.Math.MinOf() );
        STOCK_FUNCTIONS.put( "maxOf", new Math.MaxOf() );
        STOCK_FUNCTIONS.put( "abs", new Math.Abs() );
        STOCK_FUNCTIONS.put( "toInteger", new Math.toInteger() );
        STOCK_FUNCTIONS.put( "toDouble", new Math.toDouble() );
        STOCK_FUNCTIONS.put( "toLong", new Math.toLong() );
    }

    private final TemplatrCompositeSpec rootSpec;

    @SuppressWarnings( "unchecked" )
    private Templatr(Object spec, OpMode opMode, Map<String, Function> functionsMap) {
        if ( spec == null ){
            throw new SpecException( opMode.name() + " expected a spec of Map type, got 'null'." );
        }
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( opMode.name() + " expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }

        if(functionsMap == null || functionsMap.isEmpty()) {
            throw new SpecException( opMode.name() + " expected a populated functions' map type, got " + (functionsMap == null?"null":"empty") );
        }

        functionsMap = Collections.unmodifiableMap( functionsMap );
        TemplatrSpecBuilder templatrSpecBuilder = new TemplatrSpecBuilder( opMode, functionsMap );
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
            this( spec, STOCK_FUNCTIONS );
        }

        public Overwritr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.OVERWRITR, functionsMap );
        }
    }

    /**
     * This variant of templatr only writes when the key/index is missing
     */
    public static final class Definr extends Templatr {

        public Definr( final Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Definr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.DEFINER, functionsMap );
        }
    }

    /**
     * This variant of templatr only writes when the key/index is missing or the value is null
     */
    public static class Defaultr extends Templatr {

        public Defaultr( final Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Defaultr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.DEFAULTR, functionsMap );
        }
    }
}
