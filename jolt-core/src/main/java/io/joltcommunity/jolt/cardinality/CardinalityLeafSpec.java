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
package io.joltcommunity.jolt.cardinality;

import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;

import java.util.*;

/**
 * Leaf level CardinalitySpec object.
 * <p/>
 * If this CardinalitySpec's PathElement matches the input (successful parallel tree walk)
 * this CardinalitySpec has the information needed to write the given data to the output object.
 */
public class CardinalityLeafSpec extends CardinalitySpec {

    private final CardinalityRelationship cardinalityRelationship;

    public CardinalityLeafSpec(String rawKey, Object rhs) {
        super(rawKey);

        try {
            cardinalityRelationship = CardinalityRelationship.valueOf(rhs.toString());
        } catch (Exception e) {
            throw new SpecException("Invalid Cardinality type :" + rhs.toString(), e);
        }
    }

    /**
     * If this CardinalitySpec matches the inputkey, then do the work of modifying the data and return true.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    @Override
    protected boolean applyCardinality(String inputKey, Object input, WalkedPath walkedPath, Object parentContainer) {

        MatchedElement thisLevel = getMatch(inputKey, walkedPath);
        if (thisLevel == null) {
            return false;
        }
        performCardinalityAdjustment(inputKey, input, walkedPath, (Map) parentContainer, thisLevel);
        return true;
    }

    /**
     * This should only be used by composite specs with an '@' child
     *
     * @return null if no work was done, otherwise returns the re-parented data
     */
    public Object applyToParentContainer(String inputKey, Object input, WalkedPath walkedPath, Object parentContainer) {

        MatchedElement thisLevel = getMatch(inputKey, walkedPath);
        if (thisLevel == null) {
            return null;
        }
        return performCardinalityAdjustment(inputKey, input, walkedPath, (Map) parentContainer, thisLevel);
    }

    /**
     * @return null if no work was done, otherwise returns the re-parented data
     */
    private Object performCardinalityAdjustment(String inputKey, Object input, WalkedPath walkedPath, Map parentContainer, MatchedElement thisLevel) {

        // Add our the LiteralPathElement for this level, so that write path References can use it as &(0,0)
        walkedPath.add(input, thisLevel);

        Object returnValue = null;
        if (cardinalityRelationship == CardinalityRelationship.MANY) {
            if (input instanceof List) {
                returnValue = input;
            } else if (input instanceof Object[]) {
                returnValue = Arrays.asList(((Object[]) input));
            } else if (input instanceof Map || input instanceof String || input instanceof Number || input instanceof Boolean) {
                Object one = parentContainer.remove(inputKey);
                List<Object> tempList = new ArrayList<>();
                tempList.add(one);
                returnValue = tempList;

            } else if (input == null) {
                returnValue = Collections.emptyList();
            }
            parentContainer.put(inputKey, returnValue);
        } else if (cardinalityRelationship == CardinalityRelationship.ONE) {
            if (input instanceof List) {
                if (!((List<?>) input).isEmpty()) {
                    returnValue = ((List<?>) input).get(0);
                }
                parentContainer.put(inputKey, returnValue);
            } else if (input instanceof Object[]) {
                returnValue = ((Object[]) input)[0];
                parentContainer.put(inputKey, returnValue);
            }
        }

        walkedPath.removeLastElement();

        return returnValue;
    }

    private MatchedElement getMatch(String inputKey, WalkedPath walkedPath) {
        return pathElement.match(inputKey, walkedPath);
    }

    public enum CardinalityRelationship {
        ONE,
        MANY
    }
}
