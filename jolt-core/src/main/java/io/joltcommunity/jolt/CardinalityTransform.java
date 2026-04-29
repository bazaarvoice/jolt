/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt;

import io.joltcommunity.jolt.cardinality.CardinalityCompositeSpec;
import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;
import jakarta.inject.Inject;

import java.util.Map;

/**
 * The CardinalityTransform changes the cardinality of input JSON data elements.
 * The impetus for the CardinalityTransform, was to deal with data sources that are inconsistent with
 * respect to the cardinality of their returned data.
 * <p>
 * For example, say you know that there will be a "photos" element in a document. If your underlying data
 * source is trying to be nice, it may adjust the "type" of the photos element, depending on how many
 * photos there actually are.
 * <p>
 * Single photo :
 * <pre>
 *     "photos" : { "url" : "pants.com/1.jpg" }  // photos element is a "single" map entry
 * </pre>
 * <p>
 * Or multiple photos :
 * <pre>
 *     "photos" : [
 *        { "url" : "pants.com/1.jpg" },
 *        { "url" : "pants.com/2.jpg" }
 *     ]
 * </pre>
 * <p>
 * The Shiftr and Defaultr transforms can't handle that variability, so the CardinalityTransform was
 * created to "fix" document, so that the rest of the transforms can _assume_ "photos" will be an Array.
 * <p>
 * <p>
 * At a base level, a single Cardinality "command" maps data into a "ONE" or "MANY" state.
 * <p>
 * The idea is that you can start with a copy your JSON input and modify it into a Cardinality spec by
 * specifying a "cardinality" for each piece of data that you care about changing in the output.
 * Input data that are not called out in the spec will remain in the output unchanged.
 * <p>
 * For example, given this simple input JSON :
 * <pre>
 * {
 *   "review" : {
 *     "rating" : [ 5, 4 ]
 *   }
 * }
 * </pre>
 * A simple Cardinality spec could be constructed by specifying that the "rating" should be a single value:
 * <pre>
 * {
 *   "review" : {
 *     "rating" : "ONE"
 *   }
 * }
 * </pre>
 * would product the following output JSON :
 * <pre>
 * {
 *   "review" : {
 *     "rating" : 5
 *   }
 * }
 * </pre>
 * <p>
 * In this case, we turn the array "[ 5, 4 ]" into a single value by pulling the first index of the array.
 * Hence, the output has "rating : 5".
 * <p>
 * Valid Cardinality Values (RHS : right hand side)
 * <p>
 * 'ONE'
 * If the input value is a List, grab the first element in that list, and set it as the data for that element
 * For all other input value types, no-op.
 * <p>
 * 'MANY'
 * If the input is not a List, make a list and set the first element to be the input value.
 * If the input is "null", make it be an empty list.
 * If the input is a list, no-op
 * <p>
 * <p>
 * Cardinality Wildcards
 * <p>
 * As shown above, Cardinality specs can be entirely made up of literal string values, but wildcards similar
 * to some of those used by Shiftr can be used.
 * <p>
 * '*' Wildcard
 * Valid only on the LHS ( input JSON keys ) side of a Cardinality Spec
 * Unlike shiftr, the '*' wildcard can only be used by itself. It can be used
 * achieve a for/each manner of processing input.
 * <p>
 * Let's say we have the following input :
 * <pre>
 * {
 *   "photosArray" : [
 *     {
 *       "url" :  [ "http://pants.com/123-normal.jpg", "http://pants.com/123-thumbnail.jpg" ],
 *       "caption" : "Nice pants"
 *     },
 *     {
 *       "url" :  [ "http://pants.com/123-thumbnail.jpg", "http://pants.com/123-normal.jpg" ],
 *       "caption" : "Nice pants"
 *     }
 *   ]
 * }
 * </pre>
 * And we'd like a spec that says "for each item 'url', covert to ONE" :
 * <pre>
 * {
 *   "photosArray" : {
 *     "*" : { // for each item in the array
 *       "url" : "ONE"   // url should be singular
 *     }
 *   }
 * }
 * </pre>
 * Which would yield the following output :
 * <pre>
 * {
 *   "photosArray" : [
 *     {
 *       "url" :  "http://pants.com/123-normal.jpg",
 *       "caption" : "Nice pants"
 *     },
 *     {
 *       "url" :  "http://pants.com/123-thumbnail.jpg",
 *       "caption" : "Nice pants"
 *     }
 *   ]
 * }
 * </pre>
 * <p>
 * '@' Wildcard
 * Valid only on the LHS of the spec.
 * This wildcard should be used when content nested within modified content needs to be modified as well.
 * <p>
 * Let's say we have the following input:
 * <pre>
 * {
 *   "views" : [
 *     { "count" : 1024 },
 *     { "count" : 2048 }
 *   ],
 * }
 * </pre>
 * The following spec would convert "views" to a ONE and "count" to a MANY :
 * <pre>
 * {
 *   "views" : {
 *     "@" : "ONE",
 *     "count" : "MANY"
 *   }
 * }
 * </pre>
 * Yielding the following output:
 * <pre>
 * {
 *   "views" : {
 *     "count" : [ 1024 ]
 *   }
 * }
 * </pre>
 * <p>
 * <p>
 * Cardinality Logic Table
 *
 * <pre>
 * INPUT   CARDINALITY  OUTPUT   NOTE
 * String  ONE          String   no-op
 * Number  ONE          Number   no-op
 * Boolean ONE          Map      no-op
 * Map     ONE          Map      no-op
 * List    ONE          [0]      use whatever the first item in the list was
 * String  MANY         List     make the input String, be [0] in a new list
 * Number  MANY         List     make the input Number, be [0] in a new list
 * Boolean MANY         List     make the input Boolean, be [0] in a new list
 * Map     MANY         List     make the input Map, be [0] in a new list
 * List    MANY         List     no-op
 * </pre>
 */
public class CardinalityTransform implements SpecDriven, Transform {

    private final CardinalityCompositeSpec rootSpec;

    /**
     * Initialize a Cardinality transform with a CardinalityCompositeSpec.
     *
     * @throws io.joltcommunity.jolt.exception.SpecException for a malformed spec
     */
    @Inject
    public CardinalityTransform(Object spec) {

        if (spec == null) {
            throw new SpecException("CardinalityTransform expected a spec of Map type, got 'null'.");
        }
        if (!(spec instanceof Map)) {
            throw new SpecException("CardinalityTransform expected a spec of Map type, got " + spec.getClass().getSimpleName());
        }

        rootSpec = new CardinalityCompositeSpec(ROOT_KEY, (Map<String, Object>) spec);
    }


    /**
     * Applies the Cardinality transform.
     *
     * @param input the JSON object to transform
     * @return the output object with data shifted to it
     * @throws io.joltcommunity.jolt.exception.TransformException for a malformed spec or if there are issues during
     *                                                           the transform
     */
    @Override
    public Object transform(Object input) {

        rootSpec.apply(ROOT_KEY, Optional.of(input), new WalkedPath(), null, null);

        return input;
    }
}
