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

package io.joltcommunity.jolt.modifier;

import io.joltcommunity.jolt.common.spec.SpecBuilder;
import io.joltcommunity.jolt.modifier.function.Function;
import io.joltcommunity.jolt.modifier.spec.ModifierCompositeSpec;
import io.joltcommunity.jolt.modifier.spec.ModifierLeafSpec;
import io.joltcommunity.jolt.modifier.spec.ModifierSpec;

import java.util.Map;

public class ModifierSpecBuilder extends SpecBuilder<ModifierSpec> {

    public static final String CARET = "^";
    public static final String AT = "@";
    public static final String FUNCTION = "=";

    private final OpMode opMode;
    private final Map<String, Function> functionsMap;


    public ModifierSpecBuilder(OpMode opMode, Map<String, Function> functionsMap) {
        this.opMode = opMode;
        this.functionsMap = functionsMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ModifierSpec createSpec(final String lhs, final Object rhs) {
        if (rhs instanceof Map && (!((Map<?, ?>) rhs).isEmpty())) {
            return new ModifierCompositeSpec(lhs, (Map) rhs, opMode, this);
        } else {
            return new ModifierLeafSpec(lhs, rhs, opMode, functionsMap);
        }
    }
}
