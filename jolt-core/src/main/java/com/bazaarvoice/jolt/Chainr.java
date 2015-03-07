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
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Chainr is the JOLT mechanism for chaining {@link JoltTransform}s together. Any of the built-in JOLT
 * transform types can be called directly from Chainr. Any custom-written Java transforms
 * can be adapted in by implementing the {@link Transform} or {@link SpecDriven} interfaces.
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
 * - shift: ({@link Shiftr}) a tool for moving parts of an input JSON document to a new output document
 * - default: ({@link Defaultr}) a tool for applying default values to the provided JSON document
 * - remove: ({@link Removr}) a tool for removing specific values from the provided JSON document
 * - sort: ({@link Sortr}) sort the JSON document
 * - java: passes control to whatever Java class you specify as long as it implements the {@link Transform} interface
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
 * Custom Java classes that implement {@link Transform} and/or {@link SpecDriven} can be loaded by specifying the full
 *  className to load. Additionally, if upon reflection of the class we see that it is an instance of a
 *  {@link SpecDriven}, then we will construct it with a the supplied "spec" object.
 *
 * [
 *     {
 *         "operation": "com.bazaarvoice.tuna.CustomTransform",
 *
 *         "spec" : { ... } // optional spec to use to construct a custom {@link Transform} if it has the {@link SpecDriven} marker interface.
 *     },
 *     ...
 * ]
 */
public class Chainr implements Transform, ContextualTransform {

    // The list of Transforms we will march through on every call to chainr.
    // Note this will contain actual ContextualTransforms and adapted Transforms.
    private final List<ContextualTransform> transformsList;

    // The list of actual ContextualTransforms, for clients that specifically care.
    private final List<ContextualTransform> actualContextualTransforms;

    public static Chainr fromSpec( Object input ) {
        return new ChainrBuilder( input ).build();
    }

    public static Chainr fromSpec( Object input, ChainrInstantiator instantiator ) {
        return new ChainrBuilder( input ).loader( instantiator ).build();
    }

    /**
     * Adapt "normal" Transforms to look like ContextualTransforms, so that
     *  Chainr can just maintain a single list of "JoltTransforms" to run.
     */
    private static class ContextualTransformAdapter implements ContextualTransform {

        private final Transform transform;

        private ContextualTransformAdapter( Transform transform ) {
            this.transform = transform;
        }

        @Override
        public Object transform( Object input, Map<String, Object> context ) {
            return transform.transform( input );
        }
    }

    public Chainr( List<JoltTransform> joltTransforms ) {

        if ( joltTransforms == null ) {
            throw new IllegalArgumentException( "Chainr requires a list of JoltTransforms." );
        }

        transformsList = new ArrayList<>( joltTransforms.size() );
        List<ContextualTransform> realContextualTransforms = new LinkedList<>();

        for ( JoltTransform joltTransform : joltTransforms ) {

            // Do one pass of "instanceof" checks at construction time, rather than repeatedly at "runtime".
            boolean isTransform = joltTransform instanceof Transform;
            boolean isContextual = joltTransform instanceof ContextualTransform;

            if ( isContextual && isTransform ) {
                throw new SpecException( "JOLT Chainr - JoltTransform className:" + joltTransform.getClass().getCanonicalName() +
                        " implements both Transform and ContextualTransform, should only implement one of those interfaces." );
            }
            if ( ! isContextual && ! isTransform ) {
                throw new SpecException( "JOLT Chainr - Transform className:" + joltTransform.getClass().getCanonicalName() +
                        " should implement Transform or ContextualTransform." );
            }

            // We are optimizing given the assumption that Chainr objects will be built and then reused many times.
            // We want to have a single list of "transforms" that we can just blindly march through.
            // In order to accomplish this, we adapt Transforms to look like ContextualTransforms and just maintain
            //  a list of type ContextualTransform.
            if ( isContextual ) {
                transformsList.add( (ContextualTransform) joltTransform );
                realContextualTransforms.add( (ContextualTransform) joltTransform );
            }
            else
            {
                transformsList.add( new ContextualTransformAdapter( (Transform) joltTransform ) );
            }
        }

        actualContextualTransforms = Collections.unmodifiableList( realContextualTransforms );
    }

    /**
     * Runs a series of Transforms on the input, piping the inputs and outputs of the Transforms together.
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
        return doTransform( transformsList, input, context );
    }

    @Override
    public Object transform( Object input ) {
        return doTransform( transformsList, input, null );
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

        if ( from < 0 || to > transformsList.size() || to <= from ) {
            throw new TransformException( "JOLT Chainr : invalid from and to parameters : from=" + from + " to=" + to );
        }

        return doTransform( transformsList.subList( from, to ), input, context );
    }

    private static Object doTransform( List<ContextualTransform> transforms, Object input, Map<String, Object> context ) {

        Object intermediate = input;
        for ( ContextualTransform transform : transforms ) {
            intermediate = transform.transform( intermediate, context );
        }

        return intermediate;
    }

    /**
     * @return true if this Chainr instance has any ContextualTransforms
     */
    public boolean hasContextualTransforms() {
        return !actualContextualTransforms.isEmpty();
    }

    /**
     * This method allows Chainr clients to examine the ContextualTransforms
     * in this Chainr instance.  This may be helpful when building the "context".
     *
     * @return List of ContextualTransforms used by this Chainr instance
     */
    public List<ContextualTransform> getContextualTransforms() {
        return actualContextualTransforms;
    }
}
