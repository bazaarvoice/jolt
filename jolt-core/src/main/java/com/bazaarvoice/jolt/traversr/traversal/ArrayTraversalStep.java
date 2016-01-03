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

import java.util.ArrayList;
import java.util.List;

/**
 * TraversalStep that expects to handle List objects.
 */
public class ArrayTraversalStep extends BaseTraversalStep<List<Object>> {

    public ArrayTraversalStep( Traversr traversr, TraversalStep child ) {
        super( traversr, child );
    }

    public Class getStepType() {
        return List.class;
    }

    public Object newContainer() {
        return new ArrayList<>();
    }

    @Override
    public Optional<Object> get( List<Object> list, String key ) {

        int arrayIndex = Integer.parseInt( key );
        if ( arrayIndex < list.size() ) {
            return Optional.of( list.get( arrayIndex ) );
        }

        return Optional.empty();
    }

    @Override
    public Optional<Object> remove( List<Object> list, String key ) {

        int arrayIndex = Integer.parseInt( key );
        if ( arrayIndex < list.size() ) {
            return Optional.of( list.remove( arrayIndex ) );
        }

        return Optional.empty();
    }

    @Override
    public Optional<Object> overwriteSet( List<Object> list, String key, Object data ) {

        int arrayIndex = Integer.parseInt( key );
        ensureArraySize( list, arrayIndex );            // make sure it is big enough
        list.set( arrayIndex, data );
        return Optional.of( data );
    }


    private static void ensureArraySize( List<Object> list, Integer upperIndex ) {
        for ( int sizing = list.size(); sizing <= upperIndex; sizing++ ) {
            list.add( null );
        }
    }
}
