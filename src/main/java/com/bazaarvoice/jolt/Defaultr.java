package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.helpers.DefaultrKey;
import com.bazaarvoice.jolt.helpers.DefaultrKey.DefaultrKeyComparator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Defaultr : Cause its cooler without the 'e'
 * <p/>
 * Defaultr is part of the JOLT transform suite, and is intended to apply default values to JOLT output.
 * <p/>
 * Shitr walks the input data, and asks "Where should this go?"
 * Defaultr walks a spec and asks "Does this exist in the data?  If not, add it."
 * <p/>
 * Given
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
 * <p/>
 * With the desired output being
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
 * <p/>
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
 *     "*": {                       // SecondaryRatings.[anything]
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
 * <p/>
 * Defaultr only supports two wildcards "*" and "|" operators for the path walking.
 * Defaultr keys are applied from most specific to least specific :
 *   Literals
 *   ORs
 *   Stars
 * <p/>
 * 1) Walk the spec
 * 2) for each literal key in the spec (specKey)
 * 2.1) if the value of the specKey, is a literal, default the literal speckey and value into the output
 * 2.2) if the value of the specKey, is a map or array, default the speckey and an empty Map or Array into the output
 * 2.2.1) re-curse on the literal spec
 * 3) for each wildcard in the spec
 * 3.1) find all keys from the defaultee that match the wildcard
 * 3.2) treat each key as a literal spec value
 *
 * TODO doc Array Handling
 */
public class Defaultr {

    public interface WildCards {
        public static final String STAR = "*";
        public static final String OR = "|";
        public static final String ARRAY = "[]";
    }

    private DefaultrKeyComparator keyComparator = new DefaultrKeyComparator();

    /**
     * Top level Defaultr method.
     * @param spec Defaultr spec
     * @param defaultee Json object to have defaults applied to.  This will be modifed.
     * @return the modifed defaultee
     */
    public Object defaulter( Object spec, Object defaultee ) {

        // TODO : Make copy of the defaultee?

        Map<DefaultrKey, Object> keyedSpec = DefaultrKey.processSpec( (Map<String, Object>) spec );

        // Setup to call the recursive method
        DefaultrKey root = new DefaultrKey( false, "root" );
        if ( defaultee == null ) {
            defaultee = createDefaultContainerObject(root);
        }

        // Defaultr works by looking and working one level down the tree, hence we need to pass in a root and a valid defaultee
        this.applySpec( root, keyedSpec, defaultee );

        return defaultee;
    }

    /**
     * This is the main "recursive" method.   All of the inputs are never null, in that we don't recurse
     *  if spec ! instanceof Map, or there isn't a defaultee (either there was one or we created it).
     */
    private void applySpec( DefaultrKey parentKey, Map<DefaultrKey, Object> spec, Object defaultee ) {

        if ( parentKey.isArrayOutput ) {
            ensureArraySize( parentKey.maxLiteralKey, defaultee );
        }

        // Find and sort the children DefaultrKeys : literals, |, then *
        ArrayList<DefaultrKey> sortedChildren = new ArrayList<DefaultrKey>();
        sortedChildren.addAll( spec.keySet() );
        Collections.sort( sortedChildren, keyComparator );

        for ( DefaultrKey childeKey : sortedChildren ) {
            applyDefaultrKey( childeKey, spec.get( childeKey ), defaultee );
        }
    }

    private void ensureArraySize( int maxDefaultKey, Object defaultObj ) {

        if ( defaultObj instanceof List) {

            List<Object> defaultee = (List<Object>) defaultObj;

            // extend the defaultee list if needed
            for ( int index = defaultee.size() - 1; index < maxDefaultKey; index++ ) {
                defaultee.add( null );
            }
        }
    }

    private void applyDefaultrKey( DefaultrKey childKey, Object subSpec, Object defaultee ) {

        // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
        Collection literalKeys = this.findMatchingDefaulteeKeys( childKey, defaultee );

        if ( childKey.isArrayKey && defaultee instanceof List ) {
            for ( Object literalKey : literalKeys ) {
                this.defaultLiteralValue( (Integer) literalKey, childKey, subSpec, (List<Object>) defaultee );
            }
        }
        else if ( !childKey.isArrayKey && defaultee instanceof Map ) {
            for ( Object literalKey : literalKeys ) {
                this.defaultLiteralValue( (String) literalKey, childKey, subSpec, (Map<String, Object>) defaultee );
            }
        }
        // Else defaultee was not a container object, and we couldn't push values into it
    }

    /**
     * Default a literal value into a List.
     */
    private void defaultLiteralValue( Integer literalIndex, DefaultrKey dkey, Object subSpec, List<Object> defaultee ) {

        Object defaulteeValue = defaultee.get( literalIndex );

        if ( subSpec instanceof Map ) {
            if ( defaulteeValue == null ) {
                defaulteeValue = createDefaultContainerObject( dkey );
                defaultee.set( literalIndex, defaulteeValue );
            }

            // Re-curse into subspec
            this.applySpec( dkey, (Map<DefaultrKey, Object>) subSpec, defaulteeValue );
        } else {
            if ( defaulteeValue == null ) {
                // Finally, apply a default value
                defaultee.set( literalIndex, subSpec );
            }
        }
    }

    /**
     * Default into a Map
     */
    private void defaultLiteralValue( String literalKey, DefaultrKey dkey, Object subSpec, Map<String, Object> defaultee ) {

        Object defaulteeValue = defaultee.get( literalKey );

        if ( subSpec instanceof Map ) {
            if ( defaulteeValue == null ) {
                defaulteeValue = createDefaultContainerObject( dkey );
                defaultee.put( literalKey, defaulteeValue );
            }

            // Re-curse into subspec
            this.applySpec( dkey, (Map<DefaultrKey, Object>) subSpec, defaulteeValue );
        } else {
            if ( defaulteeValue == null ) {
                // Finally, apply a default value
                defaultee.put( literalKey, subSpec );
            }
        }
    }


    private Collection findMatchingDefaulteeKeys( DefaultrKey key, Object defaultee ) {

        switch ( key.op ) {
            // If the Defaultee is not null, it should get these literal values added to it
            case LITERAL:
                if ( defaultee != null ) {
                    return key.getKeyValues();
                }
                break;
            // If the Defaultee is not null, identify all its keys
            case STAR:
                if ( !key.isArrayKey && defaultee instanceof Map ) {
                    return ( (Map) defaultee ).keySet();
                }
                else if ( key.isArrayKey && defaultee instanceof List ) {
                    // this assumes the defaultee list has already been expanded to the right size
                    List defaultList = (List) defaultee;
                    List<Integer> allIndexes = new ArrayList<Integer>( defaultList.size() );
                    for ( int index = 0; index < defaultList.size(); index++ ) {
                        allIndexes.add( index );
                    }

                    return allIndexes;
                }
                break;
            // If the Defaultee is not null, identify the intersection between its keys and the OR values
            case OR:
                if ( !key.isArrayKey && defaultee instanceof Map ) {

                    Set<String> intersection = new HashSet<String>( ( (Map) defaultee ).keySet() );
                    intersection.retainAll( key.getKeyValues() );
                    return intersection;
                }
                else if ( key.isArrayKey && defaultee instanceof List ) {

                    List<Integer> indexesInRange = new ArrayList<Integer>();
                    for ( Object orValue : key.getKeyValues() ) {
                        if ( (Integer) orValue < ( (List) defaultee ).size() ) {
                            indexesInRange.add( (Integer) orValue );
                        }
                    }
                    return indexesInRange;
                }
                break;
        }

        return Collections.emptyList();
    }

    private Object createDefaultContainerObject( DefaultrKey dkey ) {
        if ( dkey.isArrayOutput ) {
            return new ArrayList<Object>();
        } else {
            return new LinkedHashMap<String, Object>();
        }
    }
}