package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.common.Placr;
import com.bazaarvoice.jolt.shiftr.Key;
import com.bazaarvoice.jolt.shiftr.Path;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Shiftr is a kind of JOLT transform that specifies where "data" from the input JSON should be placed in the
 * output JSON, aka how the input Json/data should be shifted around to make the output Json/data.
 *
 * At a base level, a single Shiftr "command" is a data mapping from an input path to an output path,
 *  similar to the "mv" command in Unix, "mv /var/data/mysql/data /media/backup/mysql".
 *
 * In Shiftr, the input path is a Json tree structure, and the output path is flattend "dot notation" path notation.
 *
 * The idea is that you can start with a copy your JSon input data data and modify it into a Shiftr spec by
 *  supplying a "dot notation" output path for each piece of data that you care about.
 *
 * For example, given this simple input Json :
 *  * <pre>
 * {
 *   "rating": {
 *       "primary": {
 *           "value": 3,
 *           "max": 5
 *       }
 *    }
 * }
 * </pre>
 * A simple Shiftr spec could be constructed by coping of that input, and modifying it to supply an output path for each piece of data :
 * <pre>
 * {
 *   "rating": {
 *     "primary": {
 *         "value": "Rating",    // the data at input path of "rating.primary.value", 3, goes to output path of "Rating"
 *         "max": "RatingRange"  // the data at input path of "rating.primary.max", 5,  goes to output path "RatingRange"
 *     }
 * }
 * </pre>
 * would product the following output Json :
 * <pre>
 * {
 *   "Rating" : 3,
 *   "RatingRange" : 5
 * }
 * </pre>
 *
 * As show above, Shiftr specs can be entirely made up of literal string values, but it's real power comes from its wildcards.
 * Using wildcards, you can leverage the fact that you know, not just the data and it's immediate key, but the whole input
 *  path to that data.
 *
 * Expanding the example above, say we have the following expanded Input Json:
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
 *       "sharpness" : {   // want output path "SecondaryRatings.sharpness.Id" = "sharpness"
 *           "value" : 7,  // want this value to goto output path "SecondaryRatings.sharpness.Value"
 *           "max" : 10    // want this value to goto output path "SecondaryRatings.sharpness.Range"
 *       }
 *   }
 * }
 * </pre>
 * The Spec would be :
 * <pre>
 * {
 *   "rating": {
 *     "primary": {
 *         "value": "Rating",                       // output -> "Rating" : 3
 *         "max": "RatingRange"                     // output -> "RatingRange" : 5
 *     },
 *     "*": {                                       // match input data like "rating.[anything-other-than-primary]"
 *         "value": "SecondaryRatings.&1.Value",    // the data at "rating.*.value" goes to "SecondaryRatings.*.Value"
 *                                                  // output -> "SecondaryRatings.quality.Value" : 3 AND
 *                                                  //           "SecondaryRatings.sharpness.Value" : 7

 *         "max": "SecondaryRatings.&1.Range",      // the data at "rating.*.max" goes to "SecondaryRatings.*.Range"
 *                                                  // output -> "SecondaryRatings.quality.Range" : 5 AND
 *                                                  //           "SecondaryRatings.sharpness.Range" : 10

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
 *   The '*' wildcard can be used by itself or to match part of a key.  This is useful for working with input JSON with keys that are "prefixed".
 *   Ex : if you had an input document like
 *   <pre>
 *       "tag-Pro" : "Awesome",
 *       "tag-Con" : "Bogus"
 *   </pre>
 *   A 'tag-*' would match both keys, and make the whole key and "*" part of the key available.
 *   Ex, input / "tag-Pro" with LHS spec "tag-*", would "tag-Pro" and "Pro" available to reference.
 *   Note the '*' wildcard is as non-greedy as possible, hence you can use more than one '*' in a key.
 *   Example, "tag-*-*" would match "tag-Foo-Bar", where
 *     &(0,0) = "tag-Foo-Bar"
 *     &(0,1) = "Foo"
 *     &(0,2) = "Bar"
 *
 * '&' Wildcard
 *   Valid on the LHS (left hand side - input JSON keys) and RHS (output data path)
 *   Means, dereference to get a value and use that value as if were a literal key.
 *   The canonical form of the wildcard is "&(0,0)".
 *   The first parameter is where in the input path to look for a value, and the second parameter is which part of the key to use (used with * key).
 *   There are syntactic sugar versions of the wildcard, all of the following mean the same thing.
 *   '&' = '&0' = '&(0)' = '&(0,0)
 *
 * '$' Wildcard
 *   Valid only on the LHS of the spec.
 *   Specifies that we want to use an input key, or input key derived value, as the data to be placed in the output JSON.
 *   The existence of this wildcard is a reflection of the fact that the "data" of the input Json, can be both in the
 *    "value", but also can be encoded in the "keys" of the input JSON (particularly with key prefix encoded scheme, like
 *     aka "tag-Pro" and "tag-Con").
 *   '$' has the same syntax as the '&' wildcard, and can be read as, dereference to get a value, and then use that value as the data to be output.
 *
 * '@' Wildcard
 *   Valid only on the LHS of the spec.
 *   For the Shiftr spec to be valid Json, it can not have two keys with the exact same value.
 *   This is problematic if you want to to both
 *     Copy/Shift an entire subobject of data to the output AND
 *     Navigate down into the subobject for other Shiftr operations.
 *
 *  Example of the problem the '@' wildcard solves
 *  <pre>
 *  {
 *     // invalid Json, two keys with the same value of "rating"
 *     "rating" : "original.payload.ratings",     // copy the whole rating sub-object to "original.payload.ratings"
 *     "rating" : {
 *       "*" : {
 *         "value": "SecondaryRatings.&1.Value",
 *         "max": "SecondaryRatings.&1.Range"
 *       }
 *     }
 *  }
 *  </pre>
 *  To address this, the spec looks like :
 *  <pre>
 *  {
 *     "rating" : {
 *       "@" : "original.payload.ratings",        // copy the whole rating sub-object to "original.payload.ratings"
 *
 *       "*" : {                                  // continue down into the subobject for matches
 *         "value": "SecondaryRatings.&1.Value",
 *         "max": "SecondaryRatings.&1.Range"
 *       }
 *     }
 *  }
 *  </pre>
 *  Thus the '@' wildcard is "copy the value of the data at this level in the tree, to the output".
 *
 * Handling JSON arrays
 *
 *  Doc Todo
 *  See / include a simplifed version of the photosArray unit test, as it deals with JSON arrays in both input and output.
 *
 * Algorithm High Level
 *  Walk the input data, and Shiftr spec simultaneously, and execute the Shiftr command/mapping each time
 *  there is a match.
 *
 * Algorithm Low Level
 * - Simultaneously walk of the spec and input JSon, and maintain a walked "input" path data structure.
 * - Determine a match between input JSON key and LHS spec, by matching LHS spec keys in the following order :
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
 * Implmentation
 *
 * Instances of this class execute Shiftr transformations given a transform spec of Jackson-style maps of maps
 * and a Jackson-style map-of-maps input.
 *
 *
 * OLD
 *
 * - if the transform has a scalar attribute for the input attribute, that scalar contains the path for putting it in the output
 * - paths contained at the scalar transform attributes are in dot-notation for referencing JSON
 * - "*" as a key matches any key that does not have its own entry
 * - "&" as a key within an object evaluates to the key that references the object
 * - "&[index]" within a path is a zero-major reference to the keys in the input document starting with current. thus &0 evaluates to the key
 *   for the current attribute, &1 evaluates to the key for the parent attribute, and so on.
 */
public class Shiftr implements Chainable {

    /**
     * Applies a Shiftr transform for Chainr
     *
     * @param input the JSON object to transform
     * @param operationEntry the JSON object from the Chainr spec containing
     *  the rest of the details necessary to carry out the transform (specifically,
     *  in this case, a shiftr spec)
     * @return the output object with data shifted to it
     * @throws JoltException for a malformed spec or if there are issues during
     * the transform
     */
    @Override
    public Object process( Object input, Map<String, Object> operationEntry ) throws JoltException {
        Object spec = operationEntry.get( "spec" );
        if (spec == null) {
            throw new JoltException( "JOLT Shiftr expected a spec in its operation entry, but instead got: "+operationEntry.toString() );
        }
        return this.xform( input, spec );
    }

    private static String ROOT_KEY = "root";

    public Object xform( Object input, Object spec ) {

        // Setup to call the recursive method
        Map<String, Object> rootedSpec = new LinkedHashMap<String, Object>();
        rootedSpec.put( ROOT_KEY, spec );

        List<Key> rootedKeyedSpec = Key.parseSpec(rootedSpec);
        Key root = rootedKeyedSpec.get(0);

        Map<String,Object> output = new LinkedHashMap<String,Object>();
        root.applyChildren( ROOT_KEY, input, new Path.LiteralPath(), output );

        return output.get( Placr.OUTPUT_PREFIX_KEY );
    }

}