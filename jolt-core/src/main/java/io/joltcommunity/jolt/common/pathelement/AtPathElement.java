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

import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;

/**
 * Path element representing the "@" key, which references the input in a Jolt transformation path.
 * Only a single "@" is allowed as the key.
 * Extends {@link BasePathElement} and implements {@link MatchablePathElement}.
 * <p>
 * It can be used on the Left hand sides of the spec only.
 */
public class AtPathElement extends BasePathElement implements MatchablePathElement {
    public AtPathElement(String key) {
        super(key);

        if (!"@".equals(key)) {
            throw new SpecException("'References Input' key '@', can only be a single '@'.  Offending key : " + key);
        }
    }

    public MatchedElement match(String dataKey, WalkedPath walkedPath) {
        return walkedPath.lastElement().getMatchedElement();  // copy what our parent was so that write keys of &0 and &1 both work.
    }

    @Override
    public String getCanonicalForm() {
        return "@";
    }
}
