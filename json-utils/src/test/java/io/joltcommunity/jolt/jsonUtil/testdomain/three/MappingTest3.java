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

import io.joltcommunity.jolt.Diffy;
import io.joltcommunity.jolt.JsonUtil;
import io.joltcommunity.jolt.JsonUtils;
import io.joltcommunity.jolt.jsonUtil.testdomain.QueryFilter;
import io.joltcommunity.jolt.jsonUtil.testdomain.QueryParam;
import io.joltcommunity.jolt.jsonUtil.testdomain.RealFilter;
import tools.jackson.core.JsonParser;
import tools.jackson.core.Version;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.node.ObjectNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class MappingTest3 {

    private Diffy diffy = new Diffy();

    public static class QueryFilterDeserializer extends ValueDeserializer<QueryFilter> {

        /**
         * Demonstrates how to do recursive polymorphic JSON deserialization in Jackson 2.2.
         *
         * Aka specify a Deserializer and "catch" some input, determine what type of Class it
         *  should be parsed too, and then reuse the Jackson infrastructure to recursively do so.
         */
        @Override
        public QueryFilter deserialize(JsonParser jp, DeserializationContext ctxt) {

            ObjectNode root = jp.readValueAsTree();

            // pass in our objectCodec so that the subJsonParser knows about our configured Modules and Annotations
            JsonParser subJsonParser = root.traverse(jp.objectReadContext());

            // Check if it is a "RealFilter"
            JsonNode queryParam = root.get("queryParam");
            if (queryParam != null && queryParam.isValueNode()) {
                return subJsonParser.readValueAs(RealFilter.class);
            }
            else {
                return subJsonParser.readValueAs(LogicalFilter3.class);
            }
        }
    }

    @Test
    public void testPolymorphicJacksonSerializationAndDeserialization()
    {

        SimpleModule testModule = new SimpleModule("testModule", new Version(1, 0, 0, null, null, null))
                .addDeserializer(QueryFilter.class, new QueryFilterDeserializer());

        ObjectMapper mapper = JsonMapper.builder()
        		.addModules(testModule)
        		.build();

        // Verifying that we can pass in a custom Mapper and create a new JsonUtil
        JsonUtil jsonUtil = JsonUtils.customJsonUtil(mapper);

        String testFixture = "/jsonUtils/testdomain/two/queryFilter-realAndLogical2.json";

        // TEST JsonUtil and our deserialization logic
        QueryFilter queryFilter = jsonUtil.classpathToType(testFixture, new TypeReference<QueryFilter>() {});

        // Make sure the hydrated QFilter looks right
        Assert.assertTrue(queryFilter instanceof LogicalFilter3);
        Assert.assertEquals(QueryParam.AND, queryFilter.queryParam());
        Assert.assertTrue(queryFilter.isLogical());
        Assert.assertEquals(3, queryFilter.filters().size());
        Assert.assertNotNull(queryFilter.filters().get(QueryParam.OR));

        // Make sure one of the top level RealFilters looks right
        QueryFilter productIdFilter = queryFilter.filters().get(QueryParam.PRODUCTID);
        Assert.assertTrue(productIdFilter.isReal());
        Assert.assertEquals(QueryParam.PRODUCTID, productIdFilter.queryParam());
        Assert.assertEquals("Acme-1234", productIdFilter.value());

        // Make sure the nested OR looks right
        QueryFilter orFilter = queryFilter.filters().get(QueryParam.OR);
        Assert.assertTrue(orFilter.isLogical());
        Assert.assertEquals(QueryParam.OR, orFilter.queryParam());
        Assert.assertEquals(2, orFilter.filters().size());

        // Make sure nested AND looks right
        QueryFilter nestedAndFilter = orFilter.filters().get(QueryParam.AND);
        Assert.assertTrue(nestedAndFilter.isLogical());
        Assert.assertEquals(QueryParam.AND, nestedAndFilter.queryParam());
        Assert.assertEquals(2, nestedAndFilter.filters().size());


        // SERIALIZE TO STRING to test serialization logic
        String unitTestString = jsonUtil.toJsonString(queryFilter);

        // LOAD and Diffy the plain vanilla JSON versions of the documents
        Map<String, Object> actual = JsonUtils.jsonToMap(unitTestString);
        Map<String, Object> expected = JsonUtils.classpathToMap(testFixture);

        // Diffy the vanilla versions
        Diffy.Result result = diffy.diff(expected, actual);
        if (!result.isEmpty()) {
            Assert.fail("Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }
}
