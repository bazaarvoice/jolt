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

package com.bazaarvoice.jolt.modifier;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.tree.WalkedPath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * From the spec we need to guess the DataType of the incoming input
 *
 * This is useful for,
 * a) in cases where the spec suggested a list but input was map
 *    and vice versa, where we can just skip processing instead of
 *    throwing random array/map errors
 * b) in case where the input is actually null and we need to create
 *    appropriate data structure and then apply spec logic
 *
 * Note: By design jolt does not stop processing on bad input data
 */
public abstract class DataType {

    private static final RUNTIME runtimeInstance = new RUNTIME();
    private static final MAP mapInstance = new MAP();

    public static DataType determineDataType( int confirmedArrayAtIndex, int confirmedMapAtIndex, int maxExplicitIndex ) {
        // based on provided flags, set appropriate dataType
        if ( confirmedArrayAtIndex > -1 ) {
            return new LIST( maxExplicitIndex );
        }
        else if ( confirmedMapAtIndex > -1 ) {
            return mapInstance;
        }
        // only a single "*" key was defined in spec. We need to get dataType at runtime from input
        else {
            return runtimeInstance;
        }
    }

    /**
     * List type that records maxIndex from spec, and uses that to expand a source (list) properly
     */
    public static final class LIST extends DataType {
        private final int maxIndexFromSpec;

        private LIST( int maxIndexFromSpec ) {
            this.maxIndexFromSpec = maxIndexFromSpec;
        }

        @Override
        protected Object createValue() {
            return new ArrayList<>();
        }

        @Override
        @SuppressWarnings( "unchecked" )
        public Integer expand( Object input ) {
            List source = (List) input;
            int reqIndex = maxIndexFromSpec;
            int currLastIndex = source.size() - 1;
            int origSize = currLastIndex + 1;
            if ( reqIndex >= source.size() ) {
                while ( currLastIndex++ < reqIndex ) {
                    source.add( null );
                }
            }
            return origSize;
        }

        @Override
        public boolean isCompatible( final Object input ) {
            return input == null || input instanceof List;
        }
    }

    /**
     * MAP type class
     */
    public static final class MAP extends DataType {
        @Override
        protected Object createValue() {
            return new LinkedHashMap<>();
        }

        @Override
        public boolean isCompatible( final Object input ) {
            return input == null || input instanceof Map;
        }
    }

    /**
     * Runtime type
     */
    public static final class RUNTIME extends DataType {
        @Override
        public boolean isCompatible( final Object input ) {
            return input != null;
        }

        @Override
        protected Object createValue() {
            throw new RuntimeException( "Cannot create for RUNTIME Type" );
        }
    }

    /**
     * Determines if an input is compatible with current DataType
     */
    public abstract boolean isCompatible(Object input);

    /**
     * MAP and LIST types overrides this method to return appropriate new map or list
     */
    protected abstract Object createValue();

    /**
     * LIST overrides this method to expand the source (list) such that in can support
     * an index specified in spec that is outside the range input list, returns original size
     * of the input
     */
    public Integer expand( Object source ) {
        throw new RuntimeException( "Expand not supported in " + this.getClass().getSimpleName() + " Type" );
    }

    /**
     * Creates an empty map/list, as required by spec, in the parent map/list at given key/index
     *
     * @param keyOrIndex of the parent object to create
     * @param walkedPath containing the parent object
     * @param opMode     to determine if this write operation is allowed
     * @return newly created object
     */
    @SuppressWarnings( "unchecked" )
    public Object create( String keyOrIndex, WalkedPath walkedPath, OpMode opMode ) {
        Object parent = walkedPath.lastElement().getTreeRef();
        Optional<Integer> origSizeOptional = walkedPath.lastElement().getOrigSize();
        int index = -1;
        try {
            index = Integer.parseInt( keyOrIndex );
        }
        catch ( Exception ignored ) {
        }
        Object value = null;
        if ( parent instanceof Map && opMode.isApplicable( (Map) parent, keyOrIndex ) ) {
            value = createValue();
            ( (Map) parent ).put( keyOrIndex, value );
        }
        else if ( parent instanceof List && opMode.isApplicable( (List) parent, index, origSizeOptional.get() ) ) {
            value = createValue();
            ( (List) parent ).set( index, value );
        }
        return value;
    }
}
