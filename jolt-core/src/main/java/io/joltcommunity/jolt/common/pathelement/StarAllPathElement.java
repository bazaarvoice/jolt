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

import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.tree.ArrayMatchedElement;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;

/**
 * PathElement for the lone "*" wildcard.   In this case we can avoid doing any
 * regex or string comparison work at all.
 */
public class StarAllPathElement implements StarPathElement {

    public StarAllPathElement(String key) {
        if (!"*".equals(key)) {
            throw new SpecException("StarAllPathElement key should just be a single '*'.  Was: " + key);
        }
    }

    /**
     * @param literal test to see if the provided string will match this Element's regex
     * @return true if the provided literal will match this Element's regex
     */
    @Override
    public boolean stringMatch(String literal) {
        return true;
    }

    @Override
    public MatchedElement match(String dataKey, WalkedPath walkedPath) {
        Optional<Integer> origSizeOptional = walkedPath.lastElement().getOrigSize();
        if (origSizeOptional.isPresent()) {
            return new ArrayMatchedElement(dataKey, origSizeOptional.get());
        } else {
            return new MatchedElement(dataKey);
        }
    }

    @Override
    public String getCanonicalForm() {
        return "*";
    }

    @Override
    public String getRawKey() {
        return "*";
    }
}
