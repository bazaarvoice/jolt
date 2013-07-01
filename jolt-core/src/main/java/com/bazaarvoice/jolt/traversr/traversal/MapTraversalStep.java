/*
 * Copyright 2013 Bazaarvoice, Inc.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.traversr.traversal;

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
    public Object get( Map<String, Object> map, String key ) {
        return map.get( key );
    }

    @Override
    public Object remove( Map<String, Object> map, String key ) {
        return map.remove( key );
    }

    @Override
    public Object overwriteSet( Map<String, Object> map, String key, Object data ) {
        map.put(  key, data );
        return data;
    }
}
