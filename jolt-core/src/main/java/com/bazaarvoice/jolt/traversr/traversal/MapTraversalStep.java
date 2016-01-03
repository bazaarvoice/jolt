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
public class MapTraversalStep extends BaseTraversalStep<Map<String,Object>> {

    public MapTraversalStep( Traversr traversr, TraversalStep child ) {
        super( traversr, child );
    }

    public Class getStepType() {
        return Map.class;
    }

    public Object newContainer() {
        return new LinkedHashMap<String, Object>();
    }

    @Override
    public Optional<Object> get( Map<String, Object> map, String key ) {

        // This here was the whole point of adding the Optional stuff.
        // Aka, I need a way to distinguish between the key not existing in the map
        //  or the key existing but having a _valid_ null value.
        if ( ! map.containsKey( key ) ) {
            return Optional.empty();
        }

        return Optional.of( map.get( key ) );
    }

    @Override
    public Optional<Object> remove( Map<String, Object> map, String key ) {
        return Optional.of( map.remove( key ) );
    }

    @Override
    public Optional<Object> overwriteSet( Map<String, Object> map, String key, Object data ) {
        map.put(  key, data );
        return Optional.of( data );
    }
}
