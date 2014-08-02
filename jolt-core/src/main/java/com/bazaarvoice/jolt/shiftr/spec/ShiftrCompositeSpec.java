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
package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.common.pathelement.*;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.WalkedPath;

import java.util.*;

/**
 * Spec that has children, which it builds and then manages during Transforms.
 */
public class ShiftrCompositeSpec extends ShiftrSpec {

    /*
    Example of how a Spec gets parsed into Composite and LeafSpec objects :

    {                                                        //  "implicit" root CompositeSpec, with one specialChild ("@") and one literalChild ("rating")
        "@" : [ "payload.original", "payload.secondCopy" ]   //  LeafSpec with an AtPathElement and outputWriters [ "payload.original", "payload.secondCopy" ]

        "rating": {                                          //  CompositeSpec with 1 literalChild ("rating") and one computedChild ("*")
            "primary": {
                "value": "Rating",
                "max": "RatingRange"
            },
            "*": {
                "value": "SecondaryRatings.&1.Value",        // LeafSpec with a LiteralPathElement and one outputWriter [ "SecondaryRatings.&1.Value" ]
                "max": "SecondaryRatings.&1.Range",
                "&": "SecondaryRatings.&1.Id"                // & with no children : specialKey : Means use the text value of the key as the input
            }
        }
    }
    */


    private static final ComputedKeysComparator computedKeysComparator = new ComputedKeysComparator();

    // Three different buckets for the children of this CompositeSpec
    private final List<ShiftrSpec> specialChildren;         // children that aren't actually triggered off the input data
    private final Map<String, ShiftrSpec> literalChildren;  // children that are simple exact matches against the input data
    private final List<ShiftrSpec> computedChildren;        // children that are regex matches against the input data

    private final ExecutionStrategy executionStrategy;

    public ShiftrCompositeSpec(String rawKey, Map<String, Object> spec ) {
        super( rawKey );

        ArrayList<ShiftrSpec> special = new ArrayList<ShiftrSpec>();
        Map<String, ShiftrSpec> literals = new LinkedHashMap<String, ShiftrSpec>();
        ArrayList<ShiftrSpec> computed = new ArrayList<ShiftrSpec>();

        // self check
        if ( pathElement instanceof AtPathElement ) {
            throw new SpecException( "@ Shiftr key, can not have children." );
        }
        if ( pathElement instanceof DollarPathElement ) {
            throw new SpecException( "$ Shiftr key, can not have children." );
        }

        List<ShiftrSpec> children = createChildren( spec );

        if ( children.isEmpty() ) {
            throw new SpecException( "Shift ShiftrSpec format error : ShiftrSpec line with empty {} as value is not valid." );
        }

        for ( ShiftrSpec child : children ) {
            if ( child.pathElement instanceof LiteralPathElement ) {
                literals.put( child.pathElement.getRawKey(), child );
            }
            // special is it is "@" or "$"
            else if ( child.pathElement instanceof AtPathElement ||
                      child.pathElement instanceof DollarPathElement ||
                      child.pathElement instanceof TransposePathElement ) {
                special.add( child );
            }
            else {   // star || (& with children)
                computed.add( child );
            }
        }

        // Only the computed children need to be sorted
        Collections.sort(computed, computedKeysComparator);

        special.trimToSize();
        computed.trimToSize();

        specialChildren = Collections.unmodifiableList( special );
        literalChildren = Collections.unmodifiableMap( literals );
        computedChildren = Collections.unmodifiableList( computed );

        executionStrategy = ExecutionStrategy.determineStrategy( literalChildren, computedChildren );
    }


    /**
     * Recursively walk the spec input tree.
     */
    private static List<ShiftrSpec> createChildren( Map<String, Object> rawSpec ) {

        List<ShiftrSpec> result = new ArrayList<ShiftrSpec>();
        Set<String> actualKeys = new HashSet<String>();

        for ( String rawLhsStr : rawSpec.keySet() ) {

            Object rawRhs = rawSpec.get( rawLhsStr );
            String[] keyStrings = rawLhsStr.split( "\\|" ); // unwrap the syntactic sugar of the OR
            for ( String keyString : keyStrings ) {

                ShiftrSpec childSpec;
                if( rawRhs instanceof Map ) {
                    childSpec = new ShiftrCompositeSpec(keyString, (Map<String, Object>) rawRhs );
                }
                else {
                    childSpec = new ShiftrLeafSpec(keyString, rawRhs );
                }

                String childCanonicalString = childSpec.pathElement.getCanonicalForm();

                if ( actualKeys.contains( childCanonicalString ) ) {
                    throw new IllegalArgumentException( "Duplicate canonical Shiftr key found : " + childCanonicalString );
                }

                actualKeys.add( childCanonicalString );

                result.add(childSpec);
            }
        }

        return result;
    }

    // visible for test
    List<ShiftrSpec> getComputedChildren() {
        return computedChildren;
    }

    /**
     * If this Spec matches the inputKey, then perform one step in the Shiftr parallel treewalk.
     *
     * Step one level down the input "tree" by carefully handling the List/Map nature the input to
     *  get the "one level down" data.
     *
     * Step one level down the Spec tree by carefully and efficiently applying our children to the
     *  "one level down" data.
     *
     * @return true if this this spec "handles" the inputKey such that no sibling specs need to see it
     */
    @Override
    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Map<String,Object> output )
    {
        LiteralPathElement thisLevel = pathElement.match( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }

        // add ourselves to the path, so that our children can reference us
        walkedPath.add( input, thisLevel );

        // Handle any special / key based children first, but don't have them block anything
        for( ShiftrSpec subSpec : specialChildren ) {
            subSpec.apply( inputKey, input, walkedPath, output );
        }

        // Handle the rest of the children
        executionStrategy.process( this, input, walkedPath, output );

        // We are done, so remove ourselves from the walkedPath
        walkedPath.removeLast();

        // we matched so increment the matchCount of our parent
        walkedPath.lastElement().getLiteralPathElement().incrementHashCount();

        return true;
    }

    private enum ExecutionStrategy {

        /**
         * The performance assumption built into this code is that the literal values in the spec, are generally smaller
         *  than the number of potential keys to check in the input.
         *
         *  More specifically, the assumption here is that the set of literalChildren is smaller than the input "keyset".
         */
        LITERALS_ONLY {
            @Override
            void processMap( ShiftrCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output ) {

                for( String key : spec.literalChildren.keySet() ) {
                    Object subInput = inputMap.get( key );

                    if ( subInput != null ) {
                        // we know the .get(key) will not return null
                        spec.literalChildren.get( key ).apply( key, subInput, walkedPath, output );
                    }
                }
            }

            @Override
            void processList( ShiftrCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output ) {

                for( String key : spec.literalChildren.keySet() ) {

                    int keyInt = Integer.MAX_VALUE;

                    try {
                        keyInt = Integer.parseInt( key );
                    }
                    catch( NumberFormatException nfe ) {
                        // If the data is an Array, but the spec keys are Non-Integer Strings,
                        //  we are annoyed, but we don't stop the whole transform.
                        // Just this part of the Transform won't work.
                    }

                    if ( keyInt < inputList.size() ) {

                        Object subInput = inputList.get( keyInt );

                        // we know the .get(key) will not return null, because we are iterating over its keys
                        spec.literalChildren.get( key ).apply( key, subInput, walkedPath, output );
                    }
                }
            }
            @Override
            void processScalar( ShiftrCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output ) {

                ShiftrSpec literalChild = spec.literalChildren.get( scalarInput );
                if ( literalChild != null ) {
                    literalChild.apply( scalarInput, null, walkedPath, output );
                }
            }
        },

        /**
         * If the CompositeSpec only has computed children, we can avoid checking the literalChildren altogether, and
         *  we can do a slightly better iteration (HashSet.entrySet) across the input.
         */
        COMPUTED_ONLY {
            @Override
            void processMap( ShiftrCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output ) {

                // Iterate over the whole entrySet rather than the keyset with follow on gets of the values
                for( Map.Entry<String, Object> inputEntry : inputMap.entrySet() ) {
                    applyKeyToComputed( spec.computedChildren, walkedPath, output, inputEntry.getKey(), inputEntry.getValue() );
                }
            }

            @Override
            void processList( ShiftrCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output ) {

                for (int index = 0; index < inputList.size(); index++) {
                    Object subInput = inputList.get( index );
                    String subKeyStr = Integer.toString( index );

                    applyKeyToComputed( spec.computedChildren, walkedPath, output, subKeyStr, subInput );
                }
            }

            @Override
            void processScalar( ShiftrCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output ) {
                applyKeyToComputed( spec.computedChildren, walkedPath, output, scalarInput, null );
            }
        },

        /**
         * In order to implement the Shiftr key precedence order, we have to process each input "key", first to
         *  see if it matches any literals, and if it does not, check against each of the computed
         */
        CONFLICT {
            @Override
            void processMap( ShiftrCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output ) {

                // Iterate over the whole entrySet rather than the keyset with follow on gets of the values
                for( Map.Entry<String, Object> inputEntry : inputMap.entrySet() ) {
                    applyKeyToLiteralAndComputed( spec, inputEntry.getKey(), inputEntry.getValue(), walkedPath, output );
                }
            }

            @Override
            void processList( ShiftrCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output ) {

                for (int index = 0; index < inputList.size(); index++) {
                    Object subInput = inputList.get( index );
                    String subKeyStr = Integer.toString( index );

                    applyKeyToLiteralAndComputed( spec, subKeyStr, subInput, walkedPath, output );
                }
            }

            @Override
            void processScalar( ShiftrCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output ) {
                applyKeyToLiteralAndComputed( spec, scalarInput, null, walkedPath, output );
            }
        },

        /**
         * We have both literal and computed children, but we have determined that there is no way an input key
         *  could match one of our literal and computed children.  Hence we can safely run each one.
         */
        NO_CONFLICT {
            @Override
            void processMap( ShiftrCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output ) {
                LITERALS_ONLY.processMap( spec, inputMap, walkedPath, output );
                COMPUTED_ONLY.processMap( spec, inputMap, walkedPath, output );
            }

            @Override
            void processList( ShiftrCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output ) {
                LITERALS_ONLY.processList( spec, inputList, walkedPath, output );
                COMPUTED_ONLY.processList( spec, inputList, walkedPath, output );
            }

            @Override
            void processScalar( ShiftrCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output ) {
                LITERALS_ONLY.processScalar( spec, scalarInput, walkedPath, output );
                COMPUTED_ONLY.processScalar( spec, scalarInput, walkedPath, output  );
            }
        };

        public void process( ShiftrCompositeSpec spec, Object input, WalkedPath walkedPath, Map<String,Object> output ) {
            if ( input instanceof Map) {
                processMap( spec, (Map<String, Object>) input, walkedPath, output );
            }
            else if ( input instanceof List ) {
                processList( spec, (List<Object>) input, walkedPath, output );
            }
            else if ( input != null ) {
                // if not a map or list, must be a scalar
                processScalar( spec, input.toString(), walkedPath, output );
            }
        }

        abstract void processMap   ( ShiftrCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String,Object> output );
        abstract void processList  ( ShiftrCompositeSpec spec, List<Object> inputList      , WalkedPath walkedPath, Map<String,Object> output );
        abstract void processScalar( ShiftrCompositeSpec spec, String scalarInput          , WalkedPath walkedPath, Map<String,Object> output );


        /**
         * This is the method we are trying to avoid calling.  It implements the Shiftr matching behavior
         *  when we have both literal and computed children.
         *
         * For each input key, we see if it matches a literal, and it not, try to match the key with every computed child.
         *
         * Worse case : n + n * c, where
         *   n is number of input keys
         *   c is number of computed children
         */
        private static void applyKeyToLiteralAndComputed( ShiftrCompositeSpec spec, String subKeyStr, Object subInput, WalkedPath walkedPath, Map<String, Object> output ) {

            ShiftrSpec literalChild = spec.literalChildren.get( subKeyStr );

            // if the subKeyStr found a literalChild, then we do not have to try to match any of the computed ones
            if ( literalChild != null ) {
                literalChild.apply( subKeyStr, subInput, walkedPath, output );
            }
            else {
                // If no literal spec key matched, iterate through all the computedChildren
                applyKeyToComputed( spec.computedChildren, walkedPath, output, subKeyStr, subInput );
            }
        }

        private static void applyKeyToComputed( List<ShiftrSpec> computedChildren, WalkedPath walkedPath, Map<String, Object> output, String subKeyStr, Object subInput ) {

            // Iterate through all the computedChildren until we find a match
            // This relies upon the computedChildren having already been sorted in priority order
            for ( ShiftrSpec computedChild : computedChildren ) {
                // if the computed key does not match it will quickly return false
                if ( computedChild.apply( subKeyStr, subInput, walkedPath, output ) ) {
                    break;
                }
            }
        }

        public static ExecutionStrategy determineStrategy( Map<String, ShiftrSpec> literalChildren, List<ShiftrSpec> computedChildren ) {

            if ( computedChildren.isEmpty() ) {
                return ExecutionStrategy.LITERALS_ONLY;
            }
            else if ( literalChildren.isEmpty() ) {
                return ExecutionStrategy.COMPUTED_ONLY;
            }

            for ( ShiftrSpec computed : computedChildren ) {
                if ( ! ( computed.pathElement instanceof StarPathElement ) ) {
                    return ExecutionStrategy.CONFLICT;
                }

                StarPathElement starPathElement = (StarPathElement) computed.pathElement;

                for ( String literal : literalChildren.keySet() ) {
                    if ( starPathElement.stringMatch( literal ) ) {
                        return ExecutionStrategy.CONFLICT;
                    }
                }
            }

            return ExecutionStrategy.NO_CONFLICT;
        }
    }

    public static class ComputedKeysComparator implements Comparator<ShiftrSpec> {

        private static HashMap<Class, Integer> orderMap = new HashMap<Class, Integer>();

        static {
            orderMap.put( AmpPathElement.class, 1 );
            // TODO this feels weird, but it works
            orderMap.put( StarRegexPathElement.class, 2 );
            orderMap.put( StarDoublePathElement.class, 3 );
            orderMap.put( StarSinglePathElement.class, 4 );
            orderMap.put( StarAllPathElement.class, 5 );
        }

        @Override
        public int compare( ShiftrSpec a, ShiftrSpec b ) {

            PathElement ape = a.pathElement;
            PathElement bpe = b.pathElement;

            int aa = orderMap.get( ape.getClass() );
            int bb = orderMap.get( bpe.getClass() );

            int elementsEqual =  aa < bb ? -1 : aa == bb ? 0 : 1;

            if ( elementsEqual != 0 ) {
                return elementsEqual;
            }

            // At this point we have two PathElements of the same type.
            String acf = ape.getCanonicalForm();
            String bcf = bpe.getCanonicalForm();

            int alen = acf.length();
            int blen = bcf.length();

            // Sort them by length, with the longest (most specific) being first
            //  aka "rating-range-*" needs to be evaluated before "rating-*", or else "rating-*" will catch too much
            // If the lengths are equal, sort alphabetically as the last ditch deterministic behavior
            return alen > blen ? -1 : alen == blen ? acf.compareTo( bcf ) : 1;
        }
    }
}
