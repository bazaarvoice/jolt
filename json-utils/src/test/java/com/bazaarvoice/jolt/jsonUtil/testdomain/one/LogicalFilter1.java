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
package com.bazaarvoice.jolt.jsonUtil.testdomain.one;

import com.bazaarvoice.jolt.jsonUtil.testdomain.QueryFilter;
import com.bazaarvoice.jolt.jsonUtil.testdomain.QueryParam;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class LogicalFilter1 implements QueryFilter {

    private final QueryParam queryParam;
    private final Map<QueryParam, QueryFilter> filters;

    @JsonCreator
    public LogicalFilter1( @JsonProperty( "queryParam" ) QueryParam queryParam,
                           @JsonProperty( "filters" ) Map<QueryParam, QueryFilter> filters ) {
        this.queryParam = queryParam;
        this.filters = filters;
    }

    @Override
    public Map<QueryParam, QueryFilter> getFilters() {
        return filters;
    }

    @Override
    public QueryParam getQueryParam() {
        return queryParam;
    }

    @Override
    @JsonIgnore
    public String getValue() {
        return null;
    }

    @Override
    @JsonIgnore
    public boolean isLogical() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isReal() {
        return false;
    }
}
