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

import io.joltcommunity.jolt.ContextualTransform;

import java.util.Map;

public class GoodContextDrivenTransform implements ContextualTransform {

    public static final String CONTEXT_KEY = "test_context_key_1";

    private static final String STATIC_KEY = "c";

    @Override
    public Object transform(Object input, Map<String, Object> context) {

        String contextValue = (String) context.get(CONTEXT_KEY);

        ((Map) input).put(STATIC_KEY, contextValue);

        return input;
    }
}
