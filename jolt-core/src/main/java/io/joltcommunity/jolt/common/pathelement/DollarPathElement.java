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
package io.joltcommunity.jolt.common.pathelement;

import io.joltcommunity.jolt.common.reference.DollarReference;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;

/**
 * Represents a path element that uses dollar-style references for dynamic key resolution
 * within a Jolt transformation path. This element evaluates its value based on the
 * {@link DollarReference} provided in the key, allowing for flexible referencing of
 * previously matched elements in the {@link WalkedPath}.
 *
 * <p>Example usage: <code>$</code> or <code>$(0,1)</code> in a Jolt spec.</p>
 *
 * Implements:
 * <ul>
 *   <li>{@link MatchablePathElement} for matching path elements</li>
 *   <li>{@link EvaluatablePathElement} for evaluating dynamic references</li>
 * </ul>
 *
 * <p>
 * It can be used on the Left hand sides of the spec only.
 */
public class DollarPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final DollarReference dRef;

    public DollarPathElement(String key) {
        super(key);

        dRef = new DollarReference(key);
    }

    @Override
    public String getCanonicalForm() {
        return dRef.getCanonicalForm();
    }

    @Override
    public String evaluate(WalkedPath walkedPath) {
        MatchedElement pe = walkedPath.elementFromEnd(dRef.getPathIndex()).getMatchedElement();
        return pe.getSubKeyRef(dRef.getKeyGroup());
    }

    @Override
    public MatchedElement match(String dataKey, WalkedPath walkedPath) {
        String evaled = evaluate(walkedPath);
        return new MatchedElement(evaled);
    }
}
