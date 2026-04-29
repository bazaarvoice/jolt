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
package io.joltcommunity.jolt.chainr.transforms;

import io.joltcommunity.jolt.SpecDriven;
import io.joltcommunity.jolt.Transform;
import jakarta.inject.Inject;

public class GoodTestTransform implements SpecDriven, Transform {

    private final Object spec;

    @Inject
    public GoodTestTransform(Object spec) {
        this.spec = spec;
    }

    @Override
    public Object transform(Object input) {
        return new TransformTestResult(input, spec);
    }
}
