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

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;

import java.util.Map;

/**
 * Removr is a kind of JOLT transform that removes content from the input Json.
 * <p/>
 * For comparision :
 * Shitr walks the input data and asks its spec "Where should this go?"
 * Defaultr walks the spec and asks "Does this exist in the data?  If not, add it."
 *
 * While, Removr walks the spec and asks "if this exists, remove it."
 * <p/>
 * Example : Given input Json like
 * <pre>
 * {
 *   "~emVersion" : "2",
 *   "id":"123124",
 *   "productId":"31231231",
 *   "submissionId":"34343",
 *   "this" : "stays",
 *   "configured" : {
 *     "a" : "b",
 *     "c" : "d"
 *   }
 * }
 * </pre>
 * With the desired output being :
 * <pre>
 * {
 *   "id":"123124",
 *   "this" : "stays",
 *
 *   "configured" : {
 *     "a" : "b"
 *   }
 * }
 * </pre>
 * This is what the Removr Spec would look like
 * <pre>
 * {
 *   "~emVersion" : "",
 *   "productId":"",
 *   "submissionId":"",
 *
 *   "configured" : {
 *     "c" : ""
 *   }
 * }
 * </pre>
 * <p/>
 * The Spec file format for Removr is a tree Map<String, Object> objects.
 * The "Right hand side" of the of each entry is ignored/irrelevant unless it is a map,
 *  in which case Removr will recursively walk down the tree.
 * <p/>
 */
public class Removr implements SpecTransform {

    private final Map<String, Object> spec;


    public Removr( Object spec ) {
        this.spec = (Map<String, Object>) spec;
    }

    @Override
    public Object transform( Object input ) {
        return removr( spec, input );
    }

    /**
     * Recursively walk the spec and remove keys from the data.
     */
    private static Object removr( Object specObj, Object removeeObj ) {

        if ( specObj != null && removeeObj != null && specObj instanceof Map && removeeObj instanceof Map ) {
            Map<String, Object> localSpec = (Map<String, Object>) specObj;
            Map<String, Object> removee = (Map<String, Object>) removeeObj;

            for ( String nukeKey : localSpec.keySet() ) {

                Object subNuke = localSpec.get( nukeKey );
                if ( subNuke != null && subNuke instanceof Map ) {
                    removr( subNuke, removee.get( nukeKey ) );
                } else {
                    removee.remove( nukeKey );
                }
            }
        }
        return removeeObj;
    }
}
