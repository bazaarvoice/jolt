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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.convertr.Key;
import com.bazaarvoice.jolt.exception.TransformException;

/**
 * Convertr is a kind of JOLT transform that convert types for JSON values.
 * Convertr supports valid conversion between STRING, INT, DOUBLE and BOOLEAN.
 *
 * Example : Given input JSON like
 * <pre>
 * "conversion": {
 *   "num2str": {
 *     "int2str": 55,
 *     "float2str": 3.1,
 *     "boolean2str": true
 *   },
 *   "str2num": {
 *     "str2int": "55",
 *     "str2float": "3.1",
 *     "str2boolean": "true"
 *   }
 * }
 * </pre>
 * With the desired output being :
 * <pre>
 * "conversion": {
 *   "num2str": {
 *     "int2str": "55",
 *     "float2str": "3.1",
 *     "boolean2str": "true"
 *    },
 *   "str2num": {
 *      "str2int": 55,
 *      "str2float": 3.1,
 *      "str2boolean": true
 *   }
 * }
 * </pre>
 * This is what the Defaultr Spec would look like
 * <pre>
 * "conversion": {
 *   "num2str": {
 *     "int2str": "STRING",
 *     "float2str": "STRING",
 *     "boolean2str": "STRING"
 *   },
 *   "str2num": {
 *     "str2int": "INT",
 *     "str2float": "DOUBLE",
 *     "str2boolean": "BOOLEAN"
 *   }
 * }
 * </pre>
 *
 * The Spec file format for Convertr a tree Map<String, Object> objects. Convertr handles outputting
 *  of JSON Arrays via special wildcard in the Spec.
 *
 * Convertr Spec WildCards and Flag :
 * "*" aka STAR : Apply these defaults to all input keys at this level
 * "|" aka OR  : Apply these defaults to input keys, if they exist
 *
 * Algorithm
 * Convertr uses the same algorithm as Defaultr to traverse the JSON objects.
 *
 */
public class Convertr implements SpecDriven, Transform{
	public interface WildCards {
        public static final String STAR = "*";
        public static final String OR = "|";
        public static final String ARRAY = "[]";
    }

    private final Key mapRoot;
    private final Key arrayRoot;
	
	@Inject
	public Convertr(Object spec) {
		String rootString = "root";

        {
            Map<String, Object> rootSpec = new LinkedHashMap<>();
            rootSpec.put( rootString, spec );
            mapRoot = Key.parseSpec( rootSpec ).iterator().next();
        }

        {
            Map<String, Object> rootSpec = new LinkedHashMap<>();
            rootSpec.put( rootString + WildCards.ARRAY, spec );
            Key tempKey = null;
            try {
                tempKey = Key.parseSpec( rootSpec ).iterator().next();
            }
            catch ( NumberFormatException nfe ) {
                // this is fine, it means the top level spec has non numeric keys
                // if someone passes a top level array as input later we will error then
            }
            arrayRoot = tempKey;
        }
	}

	public Object transform(Object input) {
		if ( input == null ) {
            // if null, assume HashMap
            input = new HashMap();
        }

        if ( input instanceof List ) {
            if  ( arrayRoot == null ) {
                throw new TransformException( "The Spec provided can not handle input that is a top level Json Array." );
            }
            arrayRoot.applyChildren( input );
        }
        else {
            mapRoot.applyChildren( input );
        }

        return input;
	}
}
