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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Tell Jackson to use the "type" field to know which subclass to initialize.
 *
 * E.g. "type" : "INTEGER" --> Deserialize a IntegerRealFilter5
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,include=JsonTypeInfo.As.PROPERTY,property="type")
@JsonSubTypes({
        @JsonSubTypes.Type(value=IntegerRealFilter5.class,name="INTEGER"),
        @JsonSubTypes.Type(value=StringRealFilter5.class,name="STRING"),
        @JsonSubTypes.Type(value=DateRealFilter5.class,name="DATE"),
        @JsonSubTypes.Type(value=BooleanRealFilter5.class,name="BOOLEAN")})
public abstract class RealFilter5<T> implements QueryFilter5<T> {

    public enum Type {
        STRING,
        INTEGER,
        BOOLEAN,
        DATE
    }

    private final Field field;
    private final Operator op;

    public RealFilter5(Field field,
                       Operator op) {
        this.field = field;
        this.op = op;
    }

    public Field getField() {
        return field;
    }

    @Override
    public Operator getOperator() {
        return op;
    }

    public abstract List<T> getValues();

    public abstract Type getType();
}
