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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursively sorts all maps within a JSON object into sorted LinkedHashMaps so that serialized
 * representations are deterministic.  Useful for debugging and making test fixtures.
 *
 * The sort order is standard alphabetical ascending, with a special case for "~" prefixed keys to be bumped to the top.
 */
public class Sortr implements Transform {

    /**
     * Sorts the JSON for human readability.
     *
     * @param input the JSON object to transform in plain vanilla Jackson Map<String, Object> style
     */
    @Override
    public Object transform( Object input ) {
        return sortJson( input );
    }

    public static Object sortJson( Object obj ) {
        if ( obj instanceof Map ) {
            return sortMap( (Map<String, Object>) obj );
        } else if ( obj instanceof List ) {
            return ordered( (List<Object>) obj );
        } else {
            return obj;
        }
    }

    private static Map<String, Object> sortMap( Map<String, Object> map ) {
        List<String> keys = new ArrayList<String>( map.keySet() );
        Collections.sort( keys, jsonKeyComparator );

        LinkedHashMap<String,Object> orderedMap = new LinkedHashMap<String, Object>( map.size() );
        for ( String key : keys ) {
            orderedMap.put( key, sortJson( map.get(key) ) );
        }
        return orderedMap;
    }

    private static List<Object> ordered( List<Object> list ) {
        // don't sort the list because that would change intent, but sort its components
        for ( int index = 0; index < list.size(); index++ ) {
            Object obj = list.get( index );
            list.set( index, sortJson( obj ) );
        }
        return list;
    }

    private static JsonKeyComparator jsonKeyComparator = new JsonKeyComparator();

    /**
     * Standard alphabetical sort, with a special case for keys beginning with "~".
     */
    private static class JsonKeyComparator implements Comparator<String> {

        @Override
        public int compare(String a, String b) {

            boolean aTilde = ( a.charAt(0) == '~' );
            boolean bTilde = ( b.charAt(0) == '~' );

            if ( aTilde && ! bTilde ) {
                return -1;
            }
            if ( ! aTilde && bTilde ) {
                return 1;
            }

            return a.compareTo( b );
        }
    }
}
