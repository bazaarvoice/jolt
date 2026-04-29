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

import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.pathelement.*;
import io.joltcommunity.jolt.common.spec.BaseSpec;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;
import io.joltcommunity.jolt.utils.StringTools;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * A Spec Object represents a single line from the JSON Cardinality Spec.
 * <p>
 * At a minimum a single Spec has :
 * Raw LHS spec value
 * Some kind of PathElement (based off that raw LHS value)
 * <p>
 * Additionally there are 2 distinct subclasses of the base Spec
 * CardinalityLeafSpec : where the RHS is either "ONE" or "MANY"
 * CardinalityCompositeSpec : where the RHS is a map of children Specs
 * <p>
 * The tree structure of formed by the CompositeSpecs is what is used during the transform
 * to do the parallel tree walk with the input data tree.
 * <p>
 * During the parallel tree walk, a Path<Literal PathElements> is maintained, and used when
 * a tree walk encounters a leaf spec.
 */
public abstract class CardinalitySpec implements BaseSpec {

    private static final String STAR = "*";
    private static final String AT = "@";

    // The processed key from the JSON config
    protected final MatchablePathElement pathElement;

    protected CardinalitySpec(String rawJsonKey) {
        this.pathElement = parse(rawJsonKey);
    }

    private static MatchablePathElement parse(String key) {
        if (key.contains(AT)) {
            return new AtPathElement(key);
        } else if (STAR.equals(key)) {
            return new StarAllPathElement(key);
        } else if (key.contains(STAR)) {
            if (StringTools.countMatches(key, STAR) == 1) {
                return new StarSinglePathElement(key);
            } else {
                return new StarRegexPathElement(key);
            }
        } else {
            return new LiteralPathElement(key);
        }
    }

    /**
     * This is the main recursive method of the CardinalityTransform parallel "spec" and "input" tree walk.
     * <p>
     * It should return true if this Spec object was able to successfully apply itself given the
     * inputKey and input object.
     * <p>
     * In the context of the CardinalityTransform parallel treewalk, if this method returns a non-null Object,
     * the assumption is that no other sibling Cardinality specs need to look at this particular input key.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    protected abstract boolean applyCardinality(String inputKey, Object input, WalkedPath walkedPath, Object parentContainer);

    @Override
    public boolean apply(final String inputKey, final Optional<Object> inputOptional, final WalkedPath walkedPath, final Map<String, Object> output, final Map<String, Object> context) {
        return applyCardinality(inputKey, inputOptional.get(), walkedPath, output);
    }

    @Override
    public MatchablePathElement getPathElement() {
        return pathElement;
    }
}
