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
package io.joltcommunity.jolt.sample;

import io.joltcommunity.jolt.Chainr;
import io.joltcommunity.jolt.JsonUtils;

import java.util.List;

public class JoltSample {

    public static void main(String[] args) {

        List<Object> chainrSpecJSON = JsonUtils.classpathToList("/json/sample/spec.json");
        Chainr chainr = Chainr.fromSpec(chainrSpecJSON);

        Object inputJSON = JsonUtils.classpathToObject("/json/sample/input.json");

        Object transformedOutput = chainr.transform(inputJSON);
        System.out.println(JsonUtils.toJsonString(transformedOutput));
    }
}
