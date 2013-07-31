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

import com.bazaarvoice.jolt.chainr.ChainrBuilder;
import com.bazaarvoice.jolt.chainr.instantiator.ChainrInstantiator;
import com.bazaarvoice.jolt.exception.TransformException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Chainr is the JOLT mechanism for chaining transforms together. Any of the built-in JOLT
 * transform types can be called directly from Chainr. Any custom-written Java transforms
 * can be adapted in by implementing the Transform or SpecTransform interfaces.
 *
 * A Chainr spec should be an array of objects in order that look like this:
 *
 * [
 *     {
 *         "operation": "[operation-name]",
 *         // stuff that the specific transform needs go here
 *     },
 *     ...
 * ]
 *
 * Each operation is called in the order that it is specified within the array. The original
 * input to Chainr is passed into the first operation, with its output passed into the next,
 * and so on. The output of the final operation is returned from Chainr.
 *
 * Currently, [operation-name] can be any of the following:
 *
 * - shift: (Shiftr) a tool for moving parts of an input JSON document to a new output document
 * - default: (Defaultr) a tool for applying default values to the provided JSON document
 * - remove: (Removr) a tool for removing specific values from the provided JSON document
 * - sort: (Sortr) sort the JSON document, purely for human readability / debugging
 * - java: passes control to whatever Java class you specify as long as it implements the Transform interface
 *
 * Shift, default, and remove operation all require a "spec", while sort does not.
 *
 * [
 *     {
 *         "operation": "shift",
 *         "spec" : { // shiftr spec }
 *     },
 *     {
 *         "operation": "sort"  // sort does not need a spec
 *     },
 *     ...
 * ]
 *
 * Custom Java classes that implement Tranform and/or SpecDriven can be loaded by specifying the full
 *  className to load.   Additionally, if upon reflection of the class we see that it is an instance of a
 *  SpecTransform, then we will construct it with a the supplied "spec" object.
 *
 * [
 *     {
 *         "operation": "com.bazaarvoice.tuna.CustomTransform",
 *
 *         "spec" : { ..  } // optional spec to use to construct a CustomTransform if it has the SpecTransform marker interface.
 *     },
 *     ...
 * ]
 */
public class Chainr implements Transform {

    public static Chainr fromSpec( Object input ) {
        return new ChainrBuilder( input ).build();
    }

    public static Chainr fromSpec( Object input, ChainrInstantiator instantiator ) {
        return new ChainrBuilder( input ).loader( instantiator ).build();
    }

    private final List<Transform> transforms;

    public Chainr( List<Transform> transforms ) {
        this.transforms = Collections.unmodifiableList( new ArrayList<Transform>( transforms ) );
    }

    /**
     * Runs a series of Transforms on the input, piping the inputs and outputs of the
     * Transforms together.
     *
     * Chainr instances are meant to be immutable once they are created so that they can be
     * used many times.
     *
     * The notion of passing "context" to the transforms allows chainr instances to be
     * reused, even in situations were you need to slightly vary.
     *
     * @param input a JSON (Jackson-parsed) maps-of-maps object to transform
     * @param context optional tweaks that the consumer of the transform would like
     * @return an object representing the JSON resulting from the transform
     * @throws com.bazaarvoice.jolt.exception.TransformException if the specification is malformed, an operation is not
     *                       found, or if one of the specified transforms throws an exception.
     */
    @Override
    public Object transform( Object input, Map<String, Object> context ) {
        return doTransform( transforms, input, context );
    }

    public Object transform( Object input ) {
        return doTransform( transforms, input, null );
    }

    /**
     * Have Chainr run a subset of the transforms in it's spec.
     *
     * Useful for testing and debugging.
     *
     * @param input the input data to transform
     * @param to transform from the chainrSpec to end with: 0 based index exclusive
     */
    public Object transform( int to, Object input ) {
        return transform( 0, to, input, null );
    }

    /**
     * Useful for testing and debugging.
     *
     * @param input the input data to transform
     * @param to transform from the chainrSpec to end with: 0 based index exclusive
     * @param context optional tweaks that the consumer of the transform would like
     */
    public Object transform( int to, Object input, Map<String, Object> context ) {
        return transform( 0, to, input, context );
    }

    /**
     * Useful for testing and debugging.
     *
     * @param input the input data to transform
     * @param from transform from the chainrSpec to start with: 0 based index
     * @param to transform from the chainrSpec to end with: 0 based index exclusive
     */
    public Object transform( int from, int to, Object input ) {
        return transform( from, to, input, null );
    }

    /**
     * Have Chainr run a subset of the transforms in it's spec.
     *
     * Useful for testing and debugging.
     *
     * @param input the input data to transform
     * @param from transform from the chainrSpec to start with: 0 based index
     * @param to transform from the chainrSpec to end with: 0 based index exclusive
     * @param context optional tweaks that the consumer of the transform would like
     */
    public Object transform( int from, int to, Object input, Map<String, Object> context ) {

        if ( (from < 0 ) || (to > transforms.size() ||  to <= from ) ) {
            throw new TransformException( "JOLT Chainr : invalid from and to parameters : from=" + from + " to=" + to );
        }

        return doTransform( transforms.subList( from, to ), input, context );
    }

    private Object doTransform( List<Transform> transformList, Object input, Map<String, Object> context ) {

        Object intermediate = input;
        for ( Transform transform : transformList ) {
            intermediate = transform.transform( intermediate, context );
        }
        return intermediate;
    }
}
