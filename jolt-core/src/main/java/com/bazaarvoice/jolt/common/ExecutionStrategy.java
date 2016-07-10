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

package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.common.spec.BaseSpec;
import com.bazaarvoice.jolt.common.spec.OrderedCompositeSpec;
import com.bazaarvoice.jolt.common.tree.WalkedPath;

import java.util.List;
import java.util.Map;

public enum ExecutionStrategy {

    /**
     * The performance assumption built into this code is that the literal values in the spec, are generally smaller
     *  than the number of potential keys to check in the input.
     *
     *  More specifically, the assumption here is that the set of literalChildren is smaller than the input "keyset".
     */
    AVAILABLE_LITERALS {
        @Override
        void processMap( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            for( String key : spec.getLiteralChildren().keySet() ) {

                // Do not work if the value is missing in the input map
                if ( inputMap.containsKey( key ) ) {

                    Optional<Object> subInputOptional = Optional.of( inputMap.get( key ) );
                    spec.getLiteralChildren().get( key ).apply( key, subInputOptional, walkedPath, output, context );
                }
            }
        }

        @Override
        void processList( OrderedCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            Integer originalSize = walkedPath.lastElement().getOrigSize().get();
            for( String key : spec.getLiteralChildren().keySet() ) {

                int keyInt = Integer.MAX_VALUE;

                try {
                    keyInt = Integer.parseInt( key );
                }
                catch( NumberFormatException nfe ) {
                    // If the data is an Array, but the spec keys are Non-Integer Strings,
                    //  we are annoyed, but we don't stop the whole transform.
                    // Just this part of the Transform won't work.
                }

                // Do not work if the index is outside of the input list
                if ( keyInt < inputList.size() ) {

                    Object subInput = inputList.get( keyInt );
                    Optional<Object> subInputOptional;
                    if ( subInput == null && originalSize != null && keyInt >= originalSize ) {
                        subInputOptional = Optional.empty();
                    }
                    else {
                        subInputOptional = Optional.of( subInput );
                    }

                    // we know the .get(key) will not return null, because we are iterating over its keys
                    spec.getLiteralChildren().get( key ).apply( key, subInputOptional, walkedPath, output, context );
                }
            }
        }

        @Override
        void processScalar( OrderedCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            BaseSpec literalChild = spec.getLiteralChildren().get( scalarInput );
            if ( literalChild != null ) {
                literalChild.apply( scalarInput, Optional.empty(), walkedPath, output, context );
            }
        }
    },

    /**
     * This is identical to AVAILABLE_LITERALS, except for the fact that it does not skip keys if its missing in the input, like literal does
     * Given this works like defaultr, a missing key is our point of entry to insert a default value, either from a passed context or a
     * hardcoded value.
     */
    ALL_LITERALS {

        @Override
        void processMap( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            for( String key : spec.getLiteralChildren().keySet() ) {

                // if the input in not available in the map us null or else get value,
                // then lookup and place a defined value from spec there
                Optional<Object> subInputOptional = Optional.empty();
                if ( inputMap.containsKey( key ) ) {
                    subInputOptional = Optional.of( inputMap.get( key ));
                }
                spec.getLiteralChildren().get( key ).apply( key, subInputOptional, walkedPath, output, context );
            }
        }

        @Override
        void processList( OrderedCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            Integer originalSize = walkedPath.lastElement().getOrigSize().get();
            for( String key : spec.getLiteralChildren().keySet() ) {

                int keyInt = Integer.MAX_VALUE;

                try {
                    keyInt = Integer.parseInt( key );
                }
                catch( NumberFormatException nfe ) {
                    // If the data is an Array, but the spec keys are Non-Integer Strings,
                    //  we are annoyed, but we don't stop the whole transform.
                    // Just this part of the Transform won't work.
                }

                // if the input in not available in the list use null or else get value,
                // then lookup and place a default value as defined in spec there
                Optional<Object> subInputOptional = Optional.empty();
                if ( keyInt < inputList.size() ) {
                    Object subInput = inputList.get( keyInt );
                    if ( subInput != null || originalSize == null || keyInt < originalSize ) {
                        subInputOptional = Optional.of( subInput );
                    }
                }
                // we know the .get(key) will not return null, because we are iterating over its keys
                spec.getLiteralChildren().get( key ).apply( key, subInputOptional, walkedPath, output, context );
            }
        }

        @Override
        void processScalar( OrderedCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            AVAILABLE_LITERALS.processScalar( spec, scalarInput, walkedPath, output, context );
        }
    },

    /**
     * If the CompositeSpec only has computed children, we can avoid checking the getLiteralChildren() altogether, and
     *  we can do a slightly better iteration (HashSet.entrySet) across the input.
     */
    COMPUTED {
        @Override
        void processMap( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            // Iterate over the whole entrySet rather than the keyset with follow on gets of the values
            for( Map.Entry<String, Object> inputEntry : inputMap.entrySet() ) {
                applyKeyToComputed( spec.getComputedChildren(), walkedPath, output, inputEntry.getKey(), Optional.of( inputEntry.getValue() ), context );
            }
        }

        @Override
        void processList( OrderedCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            Integer originalSize = walkedPath.lastElement().getOrigSize().get();
            for (int index = 0; index < inputList.size(); index++) {
                Object subInput = inputList.get( index );
                String subKeyStr = Integer.toString( index );
                Optional<Object> subInputOptional;
                if ( subInput == null && originalSize != null && index >= originalSize ) {
                    subInputOptional = Optional.empty();
                }
                else {
                    subInputOptional = Optional.of( subInput );
                }

                applyKeyToComputed( spec.getComputedChildren(), walkedPath, output, subKeyStr, subInputOptional, context );
            }
        }

        @Override
        void processScalar( OrderedCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            applyKeyToComputed( spec.getComputedChildren(), walkedPath, output, scalarInput, Optional.empty(), context );
        }
    },

    /**
     * In order to implement the key precedence order, we have to process each input "key", first to
     *  see if it matches any literals, and if it does not, check against each of the computed
     */
    CONFLICT {
        @Override
        void processMap( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            // Iterate over the whole entrySet rather than the keyset with follow on gets of the values
            for( Map.Entry<String, Object> inputEntry : inputMap.entrySet() ) {
                applyKeyToLiteralAndComputed( spec, inputEntry.getKey(), Optional.of( inputEntry.getValue() ), walkedPath, output, context );
            }
        }

        @Override
        void processList( OrderedCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

            Integer originalSize = walkedPath.lastElement().getOrigSize().get();
            for (int index = 0; index < inputList.size(); index++) {
                Object subInput = inputList.get( index );
                String subKeyStr = Integer.toString( index );
                Optional<Object> subInputOptional;
                if ( subInput == null && originalSize != null && index >= originalSize ) {
                    subInputOptional = Optional.empty();
                }
                else {
                    subInputOptional = Optional.of( subInput );
                }

                applyKeyToLiteralAndComputed( spec, subKeyStr, subInputOptional, walkedPath, output, context );
            }
        }

        @Override
        void processScalar( OrderedCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            applyKeyToLiteralAndComputed( spec, scalarInput, Optional.empty(), walkedPath, output, context );
        }
    },

    /**
     * We have both literal and computed children, but we have determined that there is no way an input key
     *  could match one of our literal and computed children.  Hence we can safely run each one.
     */
    AVAILABLE_LITERALS_WITH_COMPUTED {

        @Override
        void processMap( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            AVAILABLE_LITERALS.processMap( spec, inputMap, walkedPath, output, context );
            COMPUTED.processMap( spec, inputMap, walkedPath, output, context );
        }

        @Override
        void processList( OrderedCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            AVAILABLE_LITERALS.processList( spec, inputList, walkedPath, output, context );
            COMPUTED.processList( spec, inputList, walkedPath, output, context );
        }

        @Override
        void processScalar( OrderedCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            AVAILABLE_LITERALS.processScalar( spec, scalarInput, walkedPath, output, context );
            COMPUTED.processScalar( spec, scalarInput, walkedPath, output, context );
        }
    },

    ALL_LITERALS_WITH_COMPUTED {
        @Override
        void processMap( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            ALL_LITERALS.processMap( spec, inputMap, walkedPath, output, context );
            COMPUTED.processMap( spec, inputMap, walkedPath, output, context );
        }

        @Override
        void processList( OrderedCompositeSpec spec, List<Object> inputList, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            ALL_LITERALS.processList( spec, inputList, walkedPath, output, context );
            COMPUTED.processList( spec, inputList, walkedPath, output, context );
        }

        @Override
        void processScalar( OrderedCompositeSpec spec, String scalarInput, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {
            ALL_LITERALS.processScalar( spec, scalarInput, walkedPath, output, context );
            COMPUTED.processScalar( spec, scalarInput, walkedPath, output, context );
        }
    };

    @SuppressWarnings( "unchecked" )
    public void process( OrderedCompositeSpec spec, Optional<Object> inputOptional, WalkedPath walkedPath, Map<String,Object> output, Map<String, Object> context ) {
        Object input = inputOptional.get();
        if ( input instanceof Map) {
            processMap( spec, (Map<String, Object>) input, walkedPath, output, context );
        }
        else if ( input instanceof List ) {
            processList( spec, (List<Object>) input, walkedPath, output, context );
        }
        else if ( input != null ) {
            // if not a map or list, must be a scalar
            processScalar( spec, input.toString(), walkedPath, output, context );
        }
    }

    abstract void processMap   ( OrderedCompositeSpec spec, Map<String, Object> inputMap, WalkedPath walkedPath, Map<String,Object> output, Map<String, Object> context );
    abstract void processList  ( OrderedCompositeSpec spec, List<Object> inputList      , WalkedPath walkedPath, Map<String,Object> output, Map<String, Object> context );
    abstract void processScalar( OrderedCompositeSpec spec, String scalarInput          , WalkedPath walkedPath, Map<String,Object> output, Map<String, Object> context );


    /**
     * This is the method we are trying to avoid calling.  It implements the matching behavior
     *  when we have both literal and computed children.
     *
     * For each input key, we see if it matches a literal, and it not, try to match the key with every computed child.
     *
     * Worse case : n + n * c, where
     *   n is number of input keys
     *   c is number of computed children
     */
    private static <T extends OrderedCompositeSpec> void applyKeyToLiteralAndComputed( T spec, String subKeyStr, Optional<Object> subInputOptional, WalkedPath walkedPath, Map<String, Object> output, Map<String, Object> context ) {

        BaseSpec literalChild = spec.getLiteralChildren().get( subKeyStr );

        // if the subKeyStr found a literalChild, then we do not have to try to match any of the computed ones
        if ( literalChild != null ) {
            literalChild.apply( subKeyStr, subInputOptional, walkedPath, output, context );
        }
        else {
            // If no literal spec key matched, iterate through all the getComputedChildren()
            applyKeyToComputed( spec.getComputedChildren(), walkedPath, output, subKeyStr, subInputOptional, context );
        }
    }

    private static <T extends BaseSpec> void applyKeyToComputed( List<T> computedChildren, WalkedPath walkedPath, Map<String, Object> output, String subKeyStr, Optional<Object> subInputOptional, Map<String, Object> context ) {

        // Iterate through all the getComputedChildren() until we find a match
        // This relies upon the getComputedChildren() having already been sorted in priority order
        for ( BaseSpec computedChild : computedChildren ) {
            // if the computed key does not match it will quickly return false
            if ( computedChild.apply( subKeyStr, subInputOptional, walkedPath, output, context ) ) {
                break;
            }
        }
    }
}
