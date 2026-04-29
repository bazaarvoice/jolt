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
package io.joltcommunity.jolt.jsonUtil.testdomain.four;

import io.joltcommunity.jolt.jsonUtil.testdomain.QueryParam;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = LogicalFilter4.LogicalFilter4Serializer.class)
@JsonDeserialize(using = LogicalFilter4.LogicalFilter4Deserializer.class)
public class LogicalFilter4 implements QueryFilter4 {

    public static class LogicalFilter4Serializer extends ValueSerializer<LogicalFilter4> {
        @Override
        public void serialize(LogicalFilter4 filter, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
            jgen.writeStartObject();
            jgen.writePOJOProperty(filter.getQueryParam().toString(), filter.getFilters().values());
            jgen.writeEndObject();
        }
    }

    public static class LogicalFilter4Deserializer extends ValueDeserializer<LogicalFilter4> {
        @Override
        public LogicalFilter4 deserialize(JsonParser jp, DeserializationContext ctxt) throws JacksonException {
            ObjectNode root = jp.readValueAsTree();

            // We assume it is a LogicalFilter
            Iterator<String> iter = root.propertyNames().iterator();
            String key = iter.next();
            JsonNode arrayNode = root.iterator().next();

            if (arrayNode == null || arrayNode.isMissingNode() || !arrayNode.isArray()) {
                throw new RuntimeException("Invalid format of LogicalFilter encountered.");
            }

            // pass in objectReadContext so that the subJsonParser knows about our configured Modules and Annotations
            JsonParser subJsonParser = arrayNode.traverse(jp.objectReadContext());
            List<QueryFilter4> childrenQueryFilters = subJsonParser.readValueAs(new TypeReference<List<QueryFilter4>>() {});

            return new LogicalFilter4(QueryParam.valueOf(key), childrenQueryFilters);
        }
    }

    private final QueryParam queryParam;
    private final Map<QueryParam, QueryFilter4> filters;

    public LogicalFilter4(QueryParam queryParam, List<QueryFilter4> filters) {
        this.queryParam = queryParam;
        this.filters = new LinkedHashMap<>();
        for (QueryFilter4 queryFilter : filters) {
            this.filters.put(queryFilter.getQueryParam(), queryFilter);
        }
    }

    @Override
    public Map<QueryParam, QueryFilter4> getFilters() {
        return filters;
    }

    @Override
    public QueryParam getQueryParam() {
        return queryParam;
    }

    @Override
    public boolean isLogical() {
        return true;
    }

    @Override
    public boolean isReal() {
        return false;
    }
}
