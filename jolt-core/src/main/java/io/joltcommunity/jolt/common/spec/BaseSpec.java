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

package io.joltcommunity.jolt.common.spec;

import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.pathelement.MatchablePathElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;

import java.util.Map;

/**
 * BaseSpec interface that provide a way to get its own pathElement and an apply(...)
 * method to process the spec using input, output and context
 */
public interface BaseSpec {

    /**
     * Gimme the LHS path element
     *
     * @return LHS path element for comparison
     */
    MatchablePathElement getPathElement();

    /**
     * This is the main recursive method of the Shiftr/Templatr/Cardinality parallel "spec" and "input" tree walk.
     * <p>
     * It should return true if this Spec object was able to successfully apply itself given the
     * inputKey and input object.
     * <p>
     * In the context of the Shiftr parallel treewalk, if this method returns true, the assumption
     * is that no other sibling Shiftr specs need to look at this particular input key.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    boolean apply(final String inputKey, final Optional<Object> inputOptional, final WalkedPath walkedPath, final Map<String, Object> output, final Map<String, Object> context);

}
