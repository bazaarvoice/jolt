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
package com.bazaarvoice.jolt.jsonUtil.testdomain.five;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class BooleanRealFilter5 extends RealFilter5<Boolean> {

    private final List<Boolean> values;

    @JsonCreator
    public BooleanRealFilter5(@JsonProperty("field") Field field,
                              @JsonProperty("operator") Operator op,
                              @JsonProperty("values") List<Boolean> values) {
        super(field, op);
        this.values = values;
    }

    @Override
    public List<Boolean> getValues() {
        return values;
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }
}
