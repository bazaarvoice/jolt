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
import com.bazaarvoice.jolt.removr.spec.RemovrCompositeSpec;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Removr is a kind of JOLT transform that removes content from the input JSON.
 * <p/>
 * For comparison :
 * Shiftr walks the input data and asks its spec "Where should this go?"
 * Defaultr walks the spec and asks "Does this exist in the data?  If not, add it."
 *
 * While, Removr walks the spec and asks "if this exists, remove it."
 * <p/>
 * Example : Given input JSON like
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
 *
 *  * Removr Wildcards
 *
 * '*' Wildcard
 *   Valid only on the LHS ( input JSON keys ) side of a Removr Spec
 *   The '*' wildcard can be used by itself or to match part of a key.
 *
 *   '*' wildcard by itself :
 *    To remove "all" keys under an input,  use the * by itself on the LHS.
 *    <pre>
 *    // example input
 *    {
 *     "ratings":{
 *        "Set1":{
 *           "a":"a",
 *           "b":"b"
 *        },
 *        "Set2":{
 *            "c":"c",
 *            "b":"b"
 *        }
 *      },
 *    }
 *    //desired output
 *    {
 *     "ratings":{
 *        "Set1":{
 *           "a":"a"
 *        },
 *        "Set2":{
 *            "c":"c"
 *        }
 *      },
 *    }
 *
 *    //Spec would be
 *    {
 *     "ratings":{
 *        "*":{
 *          "b":""
 *        },
 *      },
 *    }
 *    </pre>
 *    In this example, "Set1" and "Set2" under rating both have the same structure, and thus we can use the '*'
 *     to allow use to write more compact rules to remove "b" from all children under ratings. This is especially useful when we don't know
 *     how many children will  be under ratings, but we would like to nuke certain part of it across.
 *
 *   '*' wildcard as part of a key :
 *    This is useful for working with input JSON with keys that are "prefixed".
 *    Ex : if you had an input document like
 *    <pre>
 *        {
 *         "ratings_legacy":{
 *              "Set1":{
 *                  "a":"a",
 *                  "b":"b"
 *                },
 *              "Set2":{
 *                  "a":"a",
 *                   "b":"b"
 *               }
 *           }
 *
 *         "ratings_new":{
 *               "Set1":{
 *                   "a":"a",
 *                   "b":"b"
 *               },
 *               "Set2":{
 *                   "a":"a",
 *                   "b":"b"
 *               }
 *          }
 *       }
 *    </pre>
 *
 *    A 'rating_*' would match both keys. As in Shiftr wildcard matching, * wildcard is as non greedy as possible, which enable us to give more than one * in key.
 *
 *    For an ouput that removed Set1 from all ratings_* key, the spec would be,
 *     <pre>
 *        {
 *         "ratings_*":{
 *              "Set1":""
 *       }
 *    </pre>
 * <p/>
 *
 * <p>
 *  * Arrays
 *
 * Removr can also handle data in Arrays.
 *
 *  It can walk thru all the elements of an array with the "*" wildcard.
 *
 *  Additionally, it can remove individual array indicies.  To do this the LHS key
 *   must be a number but in String format.
 *
 *  Example
 *  <pre>
 *  "spec": {
 *    "array": {
 *      "0" : ""
 *    }
 *  }
 *  </pre>
 *
 *  In this case, Removr will remove the zero-th item from the input "array", which will cause data at
 *   index "1" to become the new "0".  Because of this, Remover matches all the literal/explicit
 *   indices first, sorts them from Biggest to Smallest, then does the removing.
 * <p/>
 */
public class Removr implements SpecDriven, Transform {

    private static final String ROOT_KEY = "root";
    private final RemovrCompositeSpec rootSpec;

    @Inject
    public Removr( Object spec ) {
        if ( spec == null ){
            throw new SpecException( "Removr expected a spec of Map type, got 'null'." );
        }
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( "Removr expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }

        rootSpec = new RemovrCompositeSpec( ROOT_KEY, (Map<String, Object>) spec );
    }

    /**
     * Recursively removes data from the input JSON.
     *
     * @param input the JSON object to transform in plain vanilla Jackson Map<String, Object> style
     */
    @Override
    public Object transform( Object input ) {

        // Wrap the input in a map to fool the CompositeSpec to recurse itself.
        Map<String,Object> wrappedMap = new HashMap<>();
        wrappedMap.put(ROOT_KEY, input);
        rootSpec.applyToMap( wrappedMap );
        return input;
    }
}
