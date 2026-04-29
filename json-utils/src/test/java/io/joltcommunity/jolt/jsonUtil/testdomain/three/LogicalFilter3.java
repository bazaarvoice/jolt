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
package io.joltcommunity.jolt.jsonUtil.testdomain.three;

import io.joltcommunity.jolt.jsonUtil.testdomain.QueryFilter;
import io.joltcommunity.jolt.jsonUtil.testdomain.QueryParam;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.node.ObjectNode;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = LogicalFilter3.LogicalFilter3Serializer.class)
@JsonDeserialize(using = LogicalFilter3.LogicalFilter4Deserializer.class)
public class LogicalFilter3 implements QueryFilter {

    public static class LogicalFilter3Serializer extends ValueSerializer<LogicalFilter3> {

        @Override
        public void serialize(LogicalFilter3 filter, JsonGenerator jgen, SerializationContext provider) {
            jgen.writeStartObject();
            jgen.writePOJOProperty(filter.queryParam().toString(), filter.filters().values());
            jgen.writeEndObject();
        }
    }

    public static class LogicalFilter4Deserializer extends ValueDeserializer<LogicalFilter3> {

        @Override
        public LogicalFilter3 deserialize(JsonParser jp, DeserializationContext ctxt) {

            ObjectReadContext objectCodec = jp.objectReadContext();
            ObjectNode root = jp.readValueAsTree();

            // We assume it is a LogicalFilter
            Iterator<String> iter = root.propertyNames().iterator();
            String key = iter.next();

            JsonNode arrayNode = root.iterator().next();
            if (arrayNode == null || arrayNode.isMissingNode() || ! arrayNode.isArray()) {
                throw new RuntimeException("Invalid format of LogicalFilter encountered.");
            }

            // pass in our objectCodec so that the subJsonParser knows about our configured Modules and Annotations
            JsonParser subJsonParser = arrayNode.traverse(objectCodec);
            List<QueryFilter> childrenQueryFilters = subJsonParser.readValueAs(new TypeReference<List<QueryFilter>>() {});

            return new LogicalFilter3(QueryParam.valueOf(key), childrenQueryFilters);
        }
    }

    private final QueryParam queryParam;
    private final Map<QueryParam, QueryFilter> filters;

    public LogicalFilter3(QueryParam queryParam, List<QueryFilter> filters) {
        this.queryParam = queryParam;

        this.filters = new LinkedHashMap<>();
        for (QueryFilter queryFilter : filters) {
            this.filters.put(queryFilter.queryParam(), queryFilter);
        }
    }

    @Override
    public Map<QueryParam, QueryFilter> filters() {
        return filters;
    }

    @Override
    public QueryParam queryParam() {
        return queryParam;
    }

    @Override
    public String value() {
        return null;
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

