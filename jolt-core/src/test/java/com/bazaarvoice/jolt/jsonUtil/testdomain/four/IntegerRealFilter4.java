/*
 * Copyright 2014 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt.jsonUtil.testdomain.four;

import com.bazaarvoice.jolt.jsonUtil.testdomain.QueryParam;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IntegerRealFilter4 extends BaseRealFilter4<Integer> {

    private final Integer value;

    @JsonCreator
    public IntegerRealFilter4(@JsonProperty("queryParam") QueryParam queryParam,
                              @JsonProperty("value") Integer value) {
        super(queryParam);
        this.value = value;
    }

    @Override
    public Integer getValue() {
        return value;
    }
}
