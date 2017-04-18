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

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrCompositeSpec;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * Shiftr is a kind of JOLT transform that specifies where "data" from the input JSON should be placed in the
 * output JSON, aka how the input JSON/data should be shifted around to make the output JSON/data.
 *
 * At a base level, a single Shiftr "command" is a mapping from an input path to an output path,
 *  similar to the "mv" command in Unix, "mv /var/data/mysql/data /media/backup/mysql".
 *
 * In Shiftr, the input path is a JSON tree structure, and the output path is flattened "dot notation" path notation.
 *
 * The idea is that you can start with a copy of your JSON input data and modify it into a Shiftr spec by
 *  supplying a "dot notation" output path for each piece of data that you care about.
 *
 * For example, given this simple input JSON:
 * <pre>
 * {
 *   "rating": {
 *       "quality": {
 *           "value": 3,
 *           "max": 5
 *       }
 *    }
 * }
 * </pre>
 * A simple Shiftr spec could be constructed by copying that input, and modifying it to supply an output path for each piece of data:
 * <pre>
 * {
 *   "rating": {
 *     "quality": {
 *         "value": "SecondaryRatings.quality.Value",     // copy 3 to "SecondaryRatings.quality.Value"
 *         "max": "SecondaryRatings.quality.RatingRange"  // copy 5 to "SecondaryRatings.quality.RatingRange"
 *     }
 * }
 * </pre>
 * would produce the following output JSON:
 * <pre>
 * {
 *   "SecondaryRatings": {
 *     "quality": {
 *       "Value": 3,
 *       "RatingRange": 5
 *     }
 *   }
 * }
 * </pre>
 *
 * As shown above, Shiftr specs can be entirely made up of literal string values, but its real power comes from its wildcards.
 * Using wildcards, you can leverage the fact that you know, not just the data and its immediate key, but the whole input
 *  path to that data.
 *
 * Expanding the example above, say we have the following expanded Input JSON:
 * <pre>
 * {
 *   "rating": {
 *       "primary": {
 *           "value": 3,   // want this value to goto output path "Rating"
 *           "max": 5      // want this value to goto output path "RatingRange"
 *       },
 *       "quality": {      // want output path "SecondaryRatings.quality.Id" = "quality", aka we want the value of the key to be used
 *           "value": 3,   // want this value to goto output path "SecondaryRatings.quality.Value"
 *           "max": 5      // want this value to goto output path "SecondaryRatings.quality.Range"
 *       },
 *       "sharpness": {   // want output path "SecondaryRatings.sharpness.Id" = "sharpness"
 *           "value": 7,  // want this value to goto output path "SecondaryRatings.sharpness.Value"
 *           "max": 10    // want this value to goto output path "SecondaryRatings.sharpness.Range"
 *       }
 *   }
 * }
 * </pre>
 * The Spec would be:
 * <pre>
 * {
 *   "rating": {
 *     "primary": {
 *         "value": "Rating",                       // output -> "Rating" : 3
 *         "max": "RatingRange"                     // output -> "RatingRange" : 5
 *     },
 *     "*": {                                       // match input data like "rating.[anything-other-than-primary]"
 *         "value": "SecondaryRatings.&1.Value",    // the data at "rating.*.value" goes to "SecondaryRatings.*.Value"
 *                                                  // the "&1" means use the value one level up the tree ( "quality" or "sharpness" )
 *                                                  // output -> "SecondaryRatings.quality.Value" : 3 AND
 *                                                  //           "SecondaryRatings.sharpness.Value" : 7
 *
 *         "max": "SecondaryRatings.&1.Range",      // the data at "rating.*.max" goes to "SecondaryRatings.*.Range"
 *                                                  // the "&1" means use the value one level up the tree ( "quality" or "sharpness" )
 *                                                  // output -> "SecondaryRatings.quality.Range" : 5 AND
 *                                                  //           "SecondaryRatings.sharpness.Range" : 10
 *
 *         "$": "SecondaryRatings.&1.Id"            // Special operator $ means, use the value of the input key itself as the data
 *                                                  // output -> "SecondaryRatings.quality.Id" : "quality"
 *                                                  // output -> "SecondaryRatings.sharpness.Id" : "sharpness"
 *     }
 *   }
 * }
 * </pre>
 * Yielding the following output:
 * <pre>
 * {
 *   "Rating": 3,
 *   "RatingRange": 5,
 *   "SecondaryRatings": {
 *      "quality": {
 *         "Range": 5,
 *         "Value": 3,
 *         "Id": "quality"     // the special $ operator allows us to use input key the text value of "quality", as the "Id" of the output
 *      },
 *      "sharpness": {
 *         "Range": 10,
 *         "Value": 7,
 *         "Id": "sharpness"   // the special $ operator allows us to use input key the text value of "sharpness", as the "Id" of the output
 *      }
 *   }
 * }
 * </pre>
 *
 *
 * Shiftr Wildcards
 *
 * '*' Wildcard
 *   Valid only on the LHS ( input JSON keys ) side of a Shiftr Spec
 *   The '*' wildcard can be used by itself or to match part of a key.
 *
 *   '*' wildcard by itself:
 *    As illustrated in the example above, the '*' wildcard by itself is useful for "templating" JSON maps,
 *      where each key / value has the same "format".
 *    <pre>
 *    // example input
 *    {
 *      "rating" : {
 *        "quality": {
 *          "value": 3,
 *          "max": 5
 *        },
 *        "sharpness" : {
 *          "value" : 7,
 *          "max" : 10
 *        }
 *    }
 *    </pre>
 *    In this example, "rating.quality" and "rating.sharpness" both have the same structure/format, and thus we can use the '*'
 *     to allow us to write more compact rules and avoid having to explicitly write very similar rules for both "quality" and "sharpness".
 *
 *   '*' wildcard as part of a key:
 *    This is useful for working with input JSON with keys that are "prefixed".
 *    Ex: if you had an input document like
 *    <pre>
 *    {
 *       "tag-Pro": "Awesome",
 *       "tag-Con": "Bogus"
 *    }
 *    </pre>
 *    A 'tag-*' would match both keys, and make the whole key and "matched" part of the key available.
 *    Ex, input key of "tag-Pro" with LHS spec "tag-*", would "tag-Pro" and "Pro" available to reference.
 *    Note the '*' wildcard is as non-greedy as possible, hence you can use more than one '*' in a key.
 *    For example, "tag-*-*" would match "tag-Foo-Bar", making "tag-Foo-Bar", "Foo", and "Bar" all available to reference.
 *
 * '&' Wildcard
 *   Valid on the LHS (left hand side - input JSON keys) and RHS (output data path)
 *   Means, dereference against a "path" to get a value and use that value as if were a literal key.
 *   The canonical form of the wildcard is "&(0,0)".
 *   The first parameter is where in the input path to look for a value, and the second parameter is which part of the key to use (used with * key).
 *   There are syntactic sugar versions of the wildcard, all of the following mean the same thing.
 *     Sugar : '&' = '&0' = '&(0)' = '&(0,0)
 *   The syntactic sugar versions are nice, as there are a set of data transforms that do not need to use the canonical form,
 *    eg if your input data does not have any "prefixed" keys.
 *
 *   '&' Path lookup
 *    As Shiftr processes data and walks down the spec, it maintains a data structure describing the path it has walked.
 *    The '&' wildcard can access data from that path in a 0 major, upward oriented way.
 *    Example:
 *    <pre>
 *    {
 *        "foo" : {
 *            "bar": {
 *                "baz":  // &0 = baz, &1 = bar, &2 = foo
 *            }
 *        }
 *    }
 *    </pre>
 *
 *   '&' Subkey lookup
 *    '&' subkey lookup allows us to referece the values captured by the '*' wildcard.
 *   Example, "tag-*-*" would match "tag-Foo-Bar", making
 *     &(0,0) = "tag-Foo-Bar"
 *     &(0,1) = "Foo"
 *     &(0,2) = "Bar"
 *
 * '$' Wildcard
 *   Valid only on the LHS of the spec.
 *   The existence of this wildcard is a reflection of the fact that the "data" of the input JSON, can be both in the "values"
 *    and the "keys" of the input JSON
 *
 *   The base case operation of Shiftr is to copy input JSON "values", thus we need a way to specify that we want to copy the input JSON "key" instead.
 *
 *   Thus '$' specifies that we want to use an input key, or input key derived value, as the data to be placed in the output JSON.
 *   '$' has the same syntax as the '&' wildcard, and can be read as, dereference to get a value, and then use that value as the data to be output.
 *
 *   There are two cases where this is useful
 *     1) when a "key" in the input JSON needs to be a "id" value in the output JSON, see the ' "$": "SecondaryRatings.&1.Id" ' example above.
 *     2) you want to make a list of all the input keys.
 *
 *   Example of "a list of the input keys":
 *   <pre>
 *   // input
 *   {
 *     "rating": {
 *       "primary": {
 *         "value": 3,
 *         "max": 5
 *       },
 *       "quality": {
 *         "value": 3,
 *         "max": 7
 *       }
 *     }
 *   }
 *
 *   // desired output
 *   {
 *     "ratings" : [ "primary", "quality" ]    // Aside: this is an example of implicit JSON array creation in the output which is detailed further down.
 *                                             // For now just observe that the input keys "primary" and "quality" have both made it to the output.
 *   }
 *
 *   // spec
 *   {
 *     "rating": {
 *       "*": {               // match all keys below "rating"
 *         "$": "ratings"     // output each of the "keys" to "ratings" in the output
 *       }
 *     }
 *   }
 *   </pre>
 *
 * '#' Wildcard
 *   Valid both on the LHS and RHS, but has different behavior / format on either side.
 *   The way to think of it, is that it allows you to specify a "synthentic" value, aka a value not found in the input data.
 *
 *   On the RHS of the spec, # is only valid in the the context of an array, like "[#2]".
 *   What "[#2]" means is, go up the three levels and ask that node how many matches it has had, and then use that as an index
 *    in the arrays.
 *   This means that, while Shiftr is doing its parallel tree walk of the input data and the spec, it tracks how many matches it
 *    has processed at each level of the spec tree.
 *
 *   This useful if you want to take a JSON map and turn it into a JSON array, and you do not care about the order of the array.
 *
 *   On the LHS of the spec, # allows you to specify a hard coded String to be place as a value in the output.
 *
 *   The initial use-case for this feature was to be able to process a Boolean input value, and if the value is
 *    boolean true write out the string "enabled".  Note, this was possible before, but it required two Shiftr steps.
 *
 *   <pre>
 *      Example
 *      "hidden" : {
 *          "true" : {                             // if the value of "hidden" is true
 *              "#disabled" : "clients.clientId"   // write the word "disabled" to the path "clients.clientId"
 *          }
 *      }
 *   </pre>
 *
 *
 * '|' Wildcard
 *   Valid only on the LHS of the spec.
 *   This 'or' wildcard allows you to match multiple input keys.   Useful if you don't always know exactly what your input data will be.
 *   Example Spec :
 *   <pre>
 *   {
 *     "rating|Rating" : "rating-primary"   // match "rating" or "Rating" copy the data to "rating-primary"
 *   }
 *   </pre>
 *   This is really just syntactic sugar, as the implementation really just treats the key "rating|Rating" as two keys when processing.
 *
 *
 * '@' Wildcard
 *   Valid on both sides of the spec.
 *
 *   The basic '@' on the LHS.
 *
 *   This wildcard is necessary if you want to put both the input value and the input key somewhere in the output JSON.
 *
 *  Example '@' wildcard usage :
 *  <pre>
 *  // Say we have a spec that just operates on the value of the input key "rating"
 *  {
 *     "foo" : "place.to.put.value",  // leveraging the implicit operation of Shiftr which is to operate on input JSON values
 *  }
 *
 *  // if we want to do something with the "key" as well as the value
 *  {
 *     "foo" : {
 *       "$" : "place.to.put.key",
 *       "@" : "place.to.put.value"    // '@' explicitly tell Shiftr to operate on the input JSON value of the parent key "foo"
 *     }
 *  }
 *  </pre>
 *  Thus the '@' wildcard is the mean "copy the value of the data at this level in the tree, to the output".
 *
 *  Advanced '@' sign wildcard.
 *  The format is lools like "@(3,title)", where
 *    "3" means go up the tree 3 levels and then lookup the key
 *    "title" and use the value at that key.
 *
 *  See the filter*.json and transpose*.json Unit Test fixtures.
 *
 *
 * JSON Arrays :
 *
 *  Reading from (input) and writing to (output) JSON Arrays is fully supported.
 *
 * 1) Handling Arrays in the input JSON
 *  Shiftr treats JSON arrays in the input data as Maps with numeric keys.
 *  Example :
 *  <pre>
 *    // input
 *    {
 *       "Photos": [ "AAA.jpg", "BBB.jpg" ]
 *    }
 *
 *    // spec
 *    {
 *       "Photos" :
 *       {
 *         "1" : "photo-&-url"      // Specify that we only want to operate on the 1-th index of the "Photos" input array
 *       }
 *    }
 *
 *   // output
 *   {
 *       "photo-1-url": "BBB.jpg"
 *   }
 *  </pre>
 *
 *
 * 2) Handling Arrays in the output JSON
 *  Traditional array brackets, [ ], are used to specify array index in the output JSON.
 *  []'s are only valid on the RHS of the Shiftr spec.
 *
 *  Example :
 *  <pre>
 *    // input
 *    {
 *      "photo-1-id": "327704",
 *      "photo-1-url": "http://bob.com/0001/327704/photo.jpg"
 *    }
 *
 *    // spec
 *    {
 *      "photo-1-id": "Photos[1].Id",   // Declare the "Photos" in the output to be an array,
 *      "photo-1-url": "Photos[1].Url"  // that the 1-th array location should have data
 *
 *      // same as above but more powerful
 *      // note '&' logic can be used inside the '[ ]' notation
 *      "photo-*-url": "Photos[&(0,1)].Url"
 *    }
 *
 *    // output
 *    {
 *      "Photos": [
 *        null ,                // note Photos[0] is null, because no data was pushed to it
 *        {
 *          "Id":"327704",
 *          "Url":"http://bob.com/0001/327704/photo.jpg"
 *        }
 *      ]
 *    }
 *  </pre>
 *
 *
 * 3) JSON arrays in the spec file
 * JSON Arrays in Shiftr spec are used to to specify that piece of input data should be copied to two places in the output JSON.
 * Example :
 * <pre>
 *   // input
 *   { "foo" : 3 }
 *
 *   // spec
 *   { "foo" : [ "bar", "baz" ] }    // push the 3, to both the of the output paths
 *
 *   // output
 *   {
 *     "bar" : 3,
 *     "baz" : 3
 *   }
 * </pre>
 *
 *
 * 4) Implicit Array creation in the output JSON
 *  If a spec file is configured to output multiple pieces of data to the same output location, the
 *  output location will be turned into a JSON array.
 *  Example :
 *  <pre>
 *    // input
 *    {
 *        "foo" : "bar",
 *        "tuna" : "marlin"
 *    }
 *
 *    // spec
 *    {
 *        "foo"  : "baz",
 *        "tuna" : "baz"
 *    }
 *
 *    // output
 *    {
 *        "baz" : [ "bar", "marlin" ]     // Note the order of this Array should not be relied upon
 *    }
 *  </pre>
 *
 *
 *
 *
 *
 * Algorithm High Level
 *  Walk the input data, and Shiftr spec simultaneously, and execute the Shiftr command/mapping each time
 *  there is a match.
 *
 * Algorithm Low Level
 * - Simultaneously walk of the spec and input JSon, and maintain a walked "input" path data structure.
 * - Determine a match between input JSON key and LHS spec, by matching LHS spec keys in the following order :
 * -- Note that '|' keys are are split into their subkeys, eg "literal", '*', or '&' LHS keys
 *
 * 1) Try to match the input key with "literal" spec key values
 * 2) If no literal match is found, try to match against LHS '&' computed values.
 * 2.1) For deterministic behavior, if there is more than one '&' LHS key, they are applied/matched in alphabetical order,
 *   after the '&' syntactic sugar is replaced with its canonical form.
 * 3) If no match is found, try to match against LHS keys with '*' wildcard values.
 * 3.1) For deterministic behavior, '*' wildcard keys are sorted and applied/matched in alphabetical order.
 *
 * Note, processing of the '@' and '$' LHS keys always occur if their parent's match, and do not block any other matching.
 *
 *
 * Implementation
 *
 * Instances of this class execute Shiftr transformations given a transform spec of Jackson-style maps of maps
 * and a Jackson-style map-of-maps input.
 */
public class Shiftr implements SpecDriven, Transform {

    private final ShiftrCompositeSpec rootSpec;

    /**
     * Initialize a Shiftr transform with a Spec.
     *
     * @throws com.bazaarvoice.jolt.exception.SpecException for a malformed spec
     */
    @Inject
    public Shiftr( Object spec ) {

        if ( spec == null ){
            throw new SpecException( "Shiftr expected a spec of Map type, got 'null'." );
        }
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( "Shiftr expected a spec of Map type, got " + spec.getClass().getSimpleName() );
        }

        rootSpec = new ShiftrCompositeSpec( ROOT_KEY, (Map<String, Object>) spec );
    }


    /**
     * Applies the Shiftr transform.
     *
     * @param input the JSON object to transform
     * @return the output object with data shifted to it
     * @throws com.bazaarvoice.jolt.exception.TransformException for a malformed spec or if there are issues during
     * the transform
     */
    @Override
    public Object transform( Object input ) {

        Map<String,Object> output = new HashMap<>();

        // Create a root LiteralPathElement so that # is useful at the root level
        MatchedElement rootLpe = new MatchedElement( ROOT_KEY );
        WalkedPath walkedPath = new WalkedPath();
        walkedPath.add( input, rootLpe );

        rootSpec.apply( ROOT_KEY, Optional.of( input ), walkedPath, output, null );

        return output.get( ROOT_KEY );
    }
}
