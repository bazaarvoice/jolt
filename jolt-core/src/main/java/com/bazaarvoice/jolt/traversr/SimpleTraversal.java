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
package com.bazaarvoice.jolt.traversr;

import com.bazaarvoice.jolt.common.Optional;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for use in custom Transforms.
 *
 * Allows a programmer to just provide a single "human readable path"
 *  that they will want to be able to execute against multiple trees of data.
 *
 * Internally, parses the "human readable path" into a Traversr and a set of keys,
 *  so that the user only needs to call get/set with their input tree.
 *
 * Because the path is static, it is assumed that you will always be reading and writing
 *  objects of the same type to the tree, therefore this class can take a generic
 *  parameter "K" to reduce casting.
 */
public class SimpleTraversal<DataType> {

    private final SimpleTraversr traversr;
    private final List<String> keys;

    /**
     * Google Maps.newHashMap() trick to fill in generic type
     */
    public static <T> SimpleTraversal<T> newTraversal(String humanReadablePath) {
        return new SimpleTraversal<>( humanReadablePath );
    }

    public SimpleTraversal( String humanReadablePath ) {
        traversr = new SimpleTraversr( humanReadablePath );

        String[] keysArray = humanReadablePath.split( "\\." );

        // extract the 3 from "[3]", but don't mess with "[]"
        for ( int index = 0; index < keysArray.length; index++) {

            String key = keysArray[ index ];
            if ( key.charAt( 0 ) == '[' && key.charAt( key.length() -1 ) == ']' ) {
                if ( key.length() > 2 ) {
                    keysArray[index] = key.substring( 1, key.length() - 1 );
                }
            }
        }

        keys = Arrays.asList( keysArray );
    }

    /**
     * @param tree tree of Map and List JSON structure to navigate
     * @return the object you wanted, or null if the object or any step along the path to it were not there
     */
    public Optional<DataType> get( Object tree ) {
        return (Optional<DataType>) traversr.get( tree, keys );
    }

    /**
     * @param tree tree of Map and List JSON structure to navigate
     * @param data JSON style data object you want to set
     * @return returns the data object if successfully set, otherwise null if there was a problem walking the path
     */
    public Optional<DataType> set( Object tree, DataType data ) {
        return (Optional<DataType>) traversr.set( tree, keys, data );
    }

    /**
     * @param tree tree of Map and List JSON structure to navigate
     * @return removes and returns the data object if it was able to successfully navigate to it and remove it.
     */
    public Optional<DataType> remove( Object tree ) {
        return traversr.remove( tree, keys );
    }
}
