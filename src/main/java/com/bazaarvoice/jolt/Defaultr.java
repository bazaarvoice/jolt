package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.defaultr.ArrayKey;
import com.bazaarvoice.jolt.defaultr.Key;
import com.bazaarvoice.jolt.defaultr.MapKey;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defaultr is a kind of JOLT transform that applies default values in a non-destructive way.
 *
 * For comparision :
 * Shitr walks the input data and asks its spec "Where should this go?"
 * Defaultr walks the spec and asks "Does this exist in the data?  If not, add it."
 *
 * Example : Given input Json like
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
 * The Spec file format for Defaulr a tree Map<String, Object> objects.   Defaultr handles outputing
 *  of Json Arrays via special wildcard in the Spec.
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
 * At each level in the Spec tree, Defaultr, applies Spec from most specific to least specific :
 *   Literals key values
 *   "|"
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
public class Defaultr implements Chainable {

    public interface WildCards {
        public static final String STAR = "*";
        public static final String OR = "|";
        public static final String ARRAY = "[]";
    }

    private Key.KeyPrecedenceComparator keyComparator = new Key.KeyPrecedenceComparator();

    /**
     * Applies a Defaultr transform for Chainr
     *
     * @param input the JSON object to transform
     * @param operationEntry the JSON object from the Chainr spec containing
     *  the rest of the details necessary to carry out the transform (specifically,
     *  in this case, a defaultr spec)
     * @return the output object with defaults applied to it
     * @throws JoltException for a malformed spec or if there are issues
     */
    @Override
    public Object process( Object input, Map<String, Object> operationEntry ) throws JoltException {
        Object spec = operationEntry.get( "spec" );
        if (spec == null) {
            throw new JoltException( "JOLT Defaultr expected a spec in its operation entry, but instead got: " + operationEntry.toString() );
        }
        try {
            return defaultr( spec, input);
        }
        catch( Exception e) {
            throw new JoltException( e );
        }
    }

    /**
     * Top level standalone Defaultr method.
     *
     * @param spec Defaultr spec
     * @param defaultee Json object to have defaults applied to.  This will be modifed.
     * @return the modifed defaultee
     */
    public Object defaultr( Object spec, Object defaultee ) {

        if ( defaultee == null ) {
            throw new IllegalArgumentException( "Defaultr needs to be passed a non-null input data to apply defaults to." );
        }
        // TODO : Make copy of the defaultee?

        // Due to defaultr's array syntax, we can't actually express that we expect the top level of the defaultee to be an
        //  array.   Thus we check the top level type of the defaultee, and if null throw an exception (done above).
        String rootKey = "root";
        if ( defaultee instanceof List ) {
            // Create a fake root string entry as an Array, so that proper array sizing logic will happen during the DefaultrKey.parseSpec method.
            rootKey += WildCards.ARRAY;
        }

        // Setup to call the recursive method
        Map<String, Object> rootedSpec = new LinkedHashMap<String, Object>();
        rootedSpec.put( rootKey, spec );

        Map<Key, Object> rootedKeyedSpec = Key.parseSpec( rootedSpec );
        Key root = rootedKeyedSpec.keySet().iterator().next();

        // Defaultr works by looking one level down the tree, hence we need to pass in a root and a valid defaultee
        root.applySpec( (Map<Key, Object>) rootedKeyedSpec.get( root ), defaultee );

        return defaultee;
    }
}