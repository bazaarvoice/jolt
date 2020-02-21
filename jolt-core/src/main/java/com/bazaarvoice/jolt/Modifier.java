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

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.modifier.OpMode;
import com.bazaarvoice.jolt.modifier.TemplatrSpecBuilder;
import com.bazaarvoice.jolt.modifier.function.Function;
import com.bazaarvoice.jolt.modifier.function.Lists;
import com.bazaarvoice.jolt.modifier.function.Math;
import com.bazaarvoice.jolt.modifier.function.Objects;
import com.bazaarvoice.jolt.modifier.function.Strings;
import com.bazaarvoice.jolt.modifier.spec.ModifierCompositeSpec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Base Templatr transform that to behave differently based on provided opMode
 */
public abstract class Modifier implements SpecDriven, ContextualTransform {

    private static final Map<String, Function> STOCK_FUNCTIONS = new HashMap<>(  );

    static {
        STOCK_FUNCTIONS.put( "toLower", new Strings.toLowerCase() );
        STOCK_FUNCTIONS.put( "toUpper", new Strings.toUpperCase() );
        STOCK_FUNCTIONS.put( "concat", new Strings.concat() );
        STOCK_FUNCTIONS.put( "join", new Strings.join() );
        STOCK_FUNCTIONS.put( "split", new Strings.split() );
        STOCK_FUNCTIONS.put( "substring", new Strings.substring() );
        STOCK_FUNCTIONS.put( "trim", new Strings.trim() );
        STOCK_FUNCTIONS.put( "leftPad", new Strings.leftPad() );
        STOCK_FUNCTIONS.put( "rightPad", new Strings.rightPad() );

        STOCK_FUNCTIONS.put( "min", new Math.min() );
        STOCK_FUNCTIONS.put( "max", new Math.max() );
        STOCK_FUNCTIONS.put( "abs", new Math.abs() );
        STOCK_FUNCTIONS.put( "avg", new Math.avg() );
        STOCK_FUNCTIONS.put( "intSum", new Math.intSum() );
        STOCK_FUNCTIONS.put( "doubleSum", new Math.doubleSum() );
        STOCK_FUNCTIONS.put( "longSum", new Math.longSum() );
        STOCK_FUNCTIONS.put( "intSubtract", new Math.intSubtract() );
        STOCK_FUNCTIONS.put( "doubleSubtract", new Math.doubleSubtract() );
        STOCK_FUNCTIONS.put( "longSubtract", new Math.longSubtract() );
        STOCK_FUNCTIONS.put( "divide", new Math.divide() );
        STOCK_FUNCTIONS.put( "divideAndRound", new Math.divideAndRound() );
        STOCK_FUNCTIONS.put( "multiply", new Math.multiply() );

        STOCK_FUNCTIONS.put( "toInteger", new Objects.toInteger() );
        STOCK_FUNCTIONS.put( "toDouble", new Objects.toDouble() );
        STOCK_FUNCTIONS.put( "toLong", new Objects.toLong() );
        STOCK_FUNCTIONS.put( "toBoolean", new Objects.toBoolean() );
        STOCK_FUNCTIONS.put( "toString", new Objects.toString() );
        STOCK_FUNCTIONS.put( "size", new Objects.size() );

        STOCK_FUNCTIONS.put( "squashNulls", new Objects.squashNulls() );
        STOCK_FUNCTIONS.put( "recursivelySquashNulls", new Objects.recursivelySquashNulls() );

        STOCK_FUNCTIONS.put( "noop", Function.noop );
        STOCK_FUNCTIONS.put( "isPresent", Function.isPresent );
        STOCK_FUNCTIONS.put( "notNull", Function.notNull );
        STOCK_FUNCTIONS.put( "isNull", Function.isNull );

        STOCK_FUNCTIONS.put( "firstElement", new Lists.firstElement() );
        STOCK_FUNCTIONS.put( "lastElement", new Lists.lastElement() );
        STOCK_FUNCTIONS.put( "elementAt", new Lists.elementAt() );
        STOCK_FUNCTIONS.put( "toList", new Lists.toList() );
        STOCK_FUNCTIONS.put( "sort", new Lists.sort() );
    }

    private final ModifierCompositeSpec rootSpec;

    @SuppressWarnings( "unchecked" )
    private Modifier( Object spec, OpMode opMode, Map<String, Function> functionsMap ) {
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
        rootSpec = new ModifierCompositeSpec( ROOT_KEY, (Map<String, Object>) spec, opMode, templatrSpecBuilder );
    }

    @Override
    public Object transform( final Object input, final Map<String, Object> context ) {

        Map<String, Object> contextWrapper = new HashMap<>(  );
        contextWrapper.put( ROOT_KEY, context );

        MatchedElement rootLpe = new MatchedElement( ROOT_KEY );
        WalkedPath walkedPath = new WalkedPath();
        walkedPath.add( input, rootLpe );

        rootSpec.apply( ROOT_KEY, Optional.of( input), walkedPath, null, contextWrapper );
        return input;
    }

    /**
     * This variant of modifier creates the key/index is missing,
     * and overwrites the value if present
     */
    public static final class Overwritr extends Modifier {

        public Overwritr( Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Overwritr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.OVERWRITR, functionsMap );
        }
    }

    /**
     * This variant of modifier only writes when the key/index is missing
     */
    public static final class Definr extends Modifier {

        public Definr( final Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Definr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.DEFINER, functionsMap );
        }
    }

    /**
     * This variant of modifier only writes when the key/index is missing or the value is null
     */
    public static class Defaultr extends Modifier {

        public Defaultr( final Object spec ) {
            this( spec, STOCK_FUNCTIONS );
        }

        public Defaultr( Object spec, Map<String, Function> functionsMap ) {
            super( spec, OpMode.DEFAULTR, functionsMap );
        }
    }
}
