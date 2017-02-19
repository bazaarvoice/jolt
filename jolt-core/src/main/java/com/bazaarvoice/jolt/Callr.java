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

import com.bazaarvoice.jolt.callr.CallrCompositeSpec;
import com.bazaarvoice.jolt.callr.CallrSpec;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;

import javax.inject.Inject;
import java.util.Map;


/**
 * Callr is a kind of JOLT transform that executes methods by means of reflection on the input fields.
 * It has three arguments in which you can specify:
 * 1) "~class": the class in which the method belongs to.
 * 2) "~method": the method to invoke
 * 3) "~args": params for the method to invoke (if applies)
 * <p>
 * <p>
 * For comparison :
 * Shitr walks the input data and asks its spec "Where should this go?"
 * Defaultr walks the spec and asks "Does this exist in the data?  If not, add it."
 * <p>
 * Example : Given input JSON like
 * <pre>
 * {
 *     "id": "quality",
 *     "date": "2017-01-19"
 *     "value": 5.7
 * }
 * </pre>
 * With the desired output being :
 * <pre>
 * {
 *     "id": "QUALITY",
 *     "date": "2017/01/19"
 *     "value": 5
 * }
 * </pre>
 * This is what the Callr Spec would look like
 * <pre>
 * {
 *     "id": {
 *         "~class": "java.lang.String",
 *         "~method": "toUpperCase",
 *         "~args": []
 *
 *     },
 *     "date": {
 *         "~class": "java.lang.String",
 *         "~method": "replaceAll",
 *         "~args": ["-", "/"]
 *
 *     },
 *     "value": {
 *         "~class": "java.lang.Double",
 *         "~method": "intValue",
 *         "~args": []
 *     }
 * }
 * </pre>
 */
public class Callr implements SpecDriven, Transform {

    private static final String ROOT_KEY = "root";
    private final CallrSpec rootSpec;

    @Inject
    public Callr( Object spec ) {
        if ( spec == null ) {
            throw new SpecException( "Callr expected a spec of Map type, got 'null'." );
        }
        if ( !( spec instanceof Map ) ) {
            throw new SpecException( "Callr expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }
        rootSpec = new CallrCompositeSpec( ROOT_KEY, (Map<String, Object>) spec );
    }

    @Override
    public Object transform( Object input ) {
        rootSpec.apply( ROOT_KEY, input, new WalkedPath(), null );

        return input;
    }
}
