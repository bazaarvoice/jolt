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

import com.bazaarvoice.jolt.defaultr.Key;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defaultr is a kind of JOLT transform that applies default values in a non-destructive way.
 *
 * For comparison :
 * Shitr walks the input data and asks its spec "Where should this go?"
 * Defaultr walks the spec and asks "Does this exist in the data?  If not, add it."
 *
 * Example : Given input JSON like
 * <pre>
 * {
 *   "Rating":3,
 *   "SecondaryRatings":{
 *      "quality":{
 *         "Range":7,
 *         "Value":3,
 *         "Id":"quality"
 *      },
 *      "sharpness": {
 *         "Value":4,
 *         "Id":"sharpness"
 *      }
 *   }
 * }
 * </pre>
 * With the desired output being :
 * <pre>
 * {
 *   "Rating":3,
 *   "RatingRange" : 5,
 *   "SecondaryRatings":{
 *      "quality":{
 *         "Range":7,
 *         "Value":3,
 *         "Id":"quality",
 *         "ValueLabel": null,
 *         "Label": null,
 *         "MaxLabel": "Great",
 *         "MinLabel": "Terrible",
 *         "DisplayType": "NORMAL"
 *      },
 *      "sharpness": {
 *         "Range":5,
 *         "Value":4,
 *         "Id":"sharpness",
 *         "ValueLabel": null,
 *         "Label": null,
 *         "MaxLabel": "High",
 *         "MinLabel": "Low",
 *         "DisplayType": "NORMAL"
 *      }
 *   }
 * }
 * </pre>
 * This is what the Defaultr Spec would look like
 * <pre>
 * {
 *   "RatingRange" : 5,
 *   "SecondaryRatings": {
 *     "quality|value" : {
 *        "ValueLabel": null,
 *        "Label": null,
 *        "MaxLabel": "Great",
 *        "MinLabel": "Terrible",
 *        "DisplayType": "NORMAL"
 *
 *     }
 *     "*": {
 *        "Range" : 5,
 *        "ValueLabel": null,
 *        "Label": null,
 *        "MaxLabel": "High",
 *        "MinLabel": "Low",
 *        "DisplayType": "NORMAL"
 *     }
 *   }
 * }
 * </pre>
 *
 * The Spec file format for Defaulr a tree Map<String, Object> objects.   Defaultr handles outputting
 *  of JSON Arrays via special wildcard in the Spec.
 *
 * Defaltr Spec WildCards and Flag :
 * "*" aka STAR : Apply these defaults to all input keys at this level
 * "|" aka OR  : Apply these defaults to input keys, if they exist
 * "[]" aka : Signal to Defaultr that the data for this key should be an array.
 *   This means all defaultr keys below this entry have to be "integers".
 *
 * Valid Array Specification :
 * <pre>
 * {
 *   "photos[]" : {
 *     "2" : {
 *       "url" : "http://www.bazaarvoice.com",
 *       "caption" : ""
 *     }
 *   }
 * }
 * </pre>
 *
 * An Invalid Array Specification would be :
 * <pre>
 * {
 *   "photos[]" : {
 *     "photo-id-1234" : {
 *       "url" : "http://www.bazaarvoice.com",
 *       "caption" : ""
 *     }
 *   }
 * }
 * </pre>
 *
 * Algorithm
 * Defaultr walks its Spec in a depth first way.
 * At each level in the Spec tree, Defaultr, works from most specific to least specific Spec key:
 *   Literals key values
 *   "|", sub-sorted by how many or values there, then alphabetically (for deterministic behavior)
 *   "*"
 *
 * At a given level in the Defaultr Spec tree, only literal keys force Defaultr to create new entries
 *  in the input data: either as a single literal value or adding new nested Array or Map objects.
 * The wildcard operators, are applied after the literal keys, and will not cause the those keys to be
 *  added if they are not already present in the input document (either naturally or having been defaulted
 *  in from literal spec keys).
 *
 *
 * Algorithm :
 * 1) Walk the spec
 * 2) for each literal key in the spec (specKey)
 * 2.1) if the the specKey is a map or array, and the input is null, default an empty Map or Array into the output
 * 2.2.1) re-curse on the literal spec
 * 2.2) if the the specKey is a map or array, and the input is not null, but of the "wrong" type, skip and do not recurse
 * 2.2) if the the specKey, is a literal value, default the literal and value into the output and do not recurse
 * 3) for each wildcard in the spec
 * 3.1) find all keys from the defaultee that match the wildcard
 * 3.2) treat each key as a literal speckey
 *
 * Corner Cases :
 *
 * Due to Defaultr's array syntax, we can't actually express that we expect the top level of the input to be an Array.
 * The workaround for this is that we check the type of the object that is at the root level of the input.
 * If it is a map, no problem.
 * If it is an array, we treat the "root" level of the Defaultr spec, as if it were the child of an Array type Defaultr entry.
 * To force unambiguity, Defaultr throws an Exception if the input is null.
 */
public class Defaultr implements SpecDriven, Transform {

    public interface WildCards {
        public static final String STAR = "*";
        public static final String OR = "|";
        public static final String ARRAY = "[]";
    }

    private final Key mapRoot;
    private final Key arrayRoot;

    /**
     * Configure an instance of Defaultr with a spec.
     *
     * @throws SpecException for a malformed spec or if there are issues
     */
    @Inject
    public Defaultr( Object spec ) {

        String rootString = "root";

        // Due to defaultr's array syntax, we can't actually express that we expect the top level of the defaultee to be an array, until we see the input.
        //  Thus, in order to have parsed the spec so that we can perform many transforms, we create two specs, one where the root of the input
        //   is a map, and the other where the root of the input is an array.
        // TODO : Handle arrays better, maybe by having a parent reference in the keys, or ditch the feature of having input that is at top level an array

        {
            Map<String, Object> rootSpec = new LinkedHashMap<>();
            rootSpec.put( rootString, spec );
            mapRoot = Key.parseSpec( rootSpec ).iterator().next();
        }

        //  Thus we check the top level type of the input.
        {
            Map<String, Object> rootSpec = new LinkedHashMap<>();
            rootSpec.put( rootString + WildCards.ARRAY, spec );
            Key tempKey = null;
            try {
                tempKey = Key.parseSpec( rootSpec ).iterator().next();
            }
            catch ( NumberFormatException nfe ) {
                // this is fine, it means the top level spec has non numeric keys
                //  if someone passes a top level array as input later we will error then
            }
            arrayRoot = tempKey;
        }
    }

    /**
     * Top level standalone Defaultr method.
     *
     * @param input JSON object to have defaults applied to. This will be modified.
     * @return the modified input
     */
    @Override
    public Object transform( Object input ) {

        if ( input == null ) {
            // if null, assume HashMap
            input = new HashMap();
        }

        // TODO : Make copy of the defaultee or like shiftr create a new output object
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
