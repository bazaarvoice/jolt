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
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * In comparison to the other implementations of LogicalFilter, this one is "simpler" from a Jackson perspective, aka
 *  no custom Serializer and Deserializer.
 *
 * However that simplicity comes at the "complexity" of being a Map ( extends LinkedHashMap ).
 * A) it feels odd, and
 * B) it is less memory efficient in the main line case.  Aka, we don't always convert to and from JSON, but now
 *   we are paying the computing cost of that all the time.
 *
 * In the end, I think this is an interesting take on the LogicalFilter, but I would use the
 *  @JsonSerialize and @JsonDeserialize approach in practice.
 */
public class LogicalFilter5 extends LinkedHashMap<Operator, List<QueryFilter5>> implements QueryFilter5<QueryFilter5> {

    private final Operator operator;
    private final List<QueryFilter5> filters;

    /**
     * Jackson side constructor.
     */
    @JsonCreator
    public LogicalFilter5(Map<Operator, List<QueryFilter5>> map ) {
        super(2);
        if ( map.size() != 1 ) {
            throw new IllegalArgumentException( "Map to build a LogicalFilter5 should be size 1.  Was " + map.size() );
        }

        Operator op = map.keySet().iterator().next();
        List<QueryFilter5> filters = map.values().iterator().next();

        if ( filters == null  ) {
            throw new IllegalArgumentException( "LogicalFilter5 List<QueryFilter5>> was null." );
        }

        this.operator = op;
        this.filters = filters;

        // populate the map that we are for Serialization
        super.put( operator, filters );
    }

    /**
     * Java side constructor.
     */
    public LogicalFilter5(Operator operator, List<QueryFilter5> filters) {
        super(2);
        this.operator = operator;
        this.filters = filters;

        // populate the map that we are for Serialization
        super.put( operator, filters );
    }

    @JsonIgnore
    @Override
    public List<QueryFilter5> getValues() {
        return filters;
    }

    @JsonIgnore
    @Override
    public Operator getOperator() {
        return operator;
    }
}
