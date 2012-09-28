package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.common.Placr;
import com.bazaarvoice.jolt.shiftr.Key;
import com.bazaarvoice.jolt.shiftr.Path;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Shifter is a kind of JOLT transform that specifies where in some output JSON to put data
 * from an input JSON. Instances of this class execute Shiftr transformations given a JSON
 * input and transform spec both in Jackson-style maps of maps.
 *
 * Each entry in a Shiftr transform says where to put some data in an output object, if encountered
 * in the input object. Here is an example transform spec (annotated with JSON-illegal comments):
 *
 * <pre>
 * {
 *   "rating": {
 *     "primary": {
 *         "value": "Rating",                           // rating.primary.value from the input goes to output.Rating
 *         "max": "RatingRange"                         // rating.primary.max from the input goes to output.RatingRange
 *     },
 *     "*": {                                           // rating.[anything-but-primary]
 *         "value": "SecondaryRatings.&1.Value",       // rating.[*-match].value from the input goes to output.SecondaryRatings.[*-match].Value
 *         "max": "SecondaryRatings.&1.Range",         // rating.[*-match].max from the input goes to output.SecondaryRatings.[*-match].Range
 *         "&": "SecondaryRatings.&1.Id"               // [*-match] goes to output.SecondaryRatings.[*-match].Id
 *     }
 *   }
 * }
 * </pre>
 *
 * Shiftr walks the input document, looking up each node in the transform spec and
 * making the specified updates in the output document. This is best explained with an example input file:
 *
 * <pre>
 * {                            // no rules for all input
 *   "rating": {                // no rules for rating
 *       "primary": {           // no rules for rating.primary
 *           "value": 3,        // 3 (rating.primary.value) goes to output.Rating
 *           "max": 5           // 5 (rating.primary.max) goes to output.RatingRange
 *       },
 *       "quality": {           // "quality" (rating.*.&) goes to output.SecondaryRatings.quality.Id
 *           "value": 3,        // 3 (rating.*.value) goes to output.SecondaryRatings.quality.Value
 *           "max": 7           // 7 (rating.*.max) goes to output.SecondaryRatings.quality.Range
 *       }
 *   }
 * }
 * </pre>
 *
 * Thus, applying the transform to the above input yields the following output:
 *
 * <pre>
 * {
 *   "Rating":3,
 *   "RatingRange":5,
 *   "SecondaryRatings":{
 *      "quality":{
 *         "Range":7,
 *         "Value":3,
 *         "Id":"quality"
 *      }
 *   }
 * }
 * </pre>
 *
 * Here are the rules for applying the transform:
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