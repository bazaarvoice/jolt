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
package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.Traversr;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TraversalStep that expects to handle Map objects.
 */
public class MapTraversalStep<DataType> extends BaseTraversalStep<Map<String,Object>, DataType> {

    public MapTraversalStep( Traversr traversr, TraversalStep child ) {
        super( traversr, child );
    }

    public Class<?> getStepType() {
        return Map.class;
    }

    public Map<String,Object> newContainer() {
        return new LinkedHashMap<>();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<DataType> get( Map<String, Object> map, String key ) {

        // This here was the whole point of adding the Optional stuff.
        // Aka, I need a way to distinguish between the key not existing in the map
        //  or the key existing but having a _valid_ null value.
        if ( ! map.containsKey( key ) ) {
            return Optional.empty();
        }

        return Optional.of( (DataType) map.get( key ) );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<DataType> remove( Map<String, Object> map, String key ) {
        return Optional.of( (DataType) map.remove( key ) );
    }

    @Override
    public Optional<DataType> overwriteSet( Map<String, Object> map, String key, DataType data ) {
        map.put( key, data );
        return Optional.of( data );
    }
}
