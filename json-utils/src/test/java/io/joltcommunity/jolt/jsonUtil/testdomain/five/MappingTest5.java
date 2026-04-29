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
package io.joltcommunity.jolt.jsonUtil.testdomain.five;

import io.joltcommunity.jolt.Diffy;
import io.joltcommunity.jolt.JsonUtil;
import io.joltcommunity.jolt.JsonUtils;
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

public class MappingTest5 {

    private Diffy diffy = new Diffy();

    public static class QueryFilter5Deserializer extends ValueDeserializer<QueryFilter5> {

        /**
         * I tried moving this logic to be an @JsonDeserialize on the QueryFilter5 interface
         *  but I could not get it to work.
         *
         * When this logic was moved there (and other logic was messed with), I would either get
         *  A) Deserializaion error on the List<QueryFilter5> in the LogicalFilter5 or
         *  B) stack overflow from bad recursion
         *
         * The problem with the List<QueryFilter5> in the LogicalFilter5, seemed like it
         *  looked at the type of the first QueryFilter5, and assumed that all the elements in
         *  the Array/List would be the same time, which totally breaks the goal of mixing
         *  Real and Logical QueryFilter subclasses.
         */
        @Override
        public QueryFilter5 deserialize(JsonParser jp, DeserializationContext ctxt) {

            ObjectNode root = jp.readValueAsTree();

            // pass in our objectCodec so that the subJsonParser knows about our configured Modules and Annotations
            JsonParser subJsonParser = root.traverse(jp.objectReadContext());

            // Check if it is a "RealFilter"
            JsonNode valuesParam = root.get("values");

            if (valuesParam == null) {
                 return subJsonParser.readValueAs(LogicalFilter5.class);
            }
            if (! valuesParam.isArray()) {
               throw new RuntimeException("Expected an Array");
            }

            return subJsonParser.readValueAs(RealFilter5.class);
        }
    }

    @Test
    public void testPolymorphicJacksonSerializationAndDeserialization()
    {

        SimpleModule testModule = new SimpleModule("testModule", new Version(1, 0, 0, null, null, null))
                .addDeserializer(QueryFilter5.class, new QueryFilter5Deserializer());

        ObjectMapper mapper = JsonMapper.builder()
        		.addModules(testModule)
        		.build();

        // Verifying that we can pass in a custom Mapper and create a new JsonUtil
        JsonUtil jsonUtil = JsonUtils.customJsonUtil(mapper);

        String testFixture = "/jsonUtils/testdomain/five/queryFilter-realAndLogical5.json";

        // TEST JsonUtil and our deserialization logic
        QueryFilter5 queryFilter = jsonUtil.classpathToType(testFixture, new TypeReference<QueryFilter5>() {});

        // Make sure the hydrated QFilter looks right
        Assert.assertTrue(queryFilter instanceof LogicalFilter5);
        LogicalFilter5 andFilter = (LogicalFilter5) queryFilter;
        Assert.assertEquals(Operator.AND, andFilter.getOperator());
        Assert.assertNotNull(andFilter.getValues());
        Assert.assertEquals(3, andFilter.getValues().size());

        // Make sure one of the top level RealFilters looks right
        QueryFilter5 productIdFilter = andFilter.getValues().get(1);
        Assert.assertTrue(productIdFilter instanceof StringRealFilter5);
        StringRealFilter5 stringRealProductIdFilter = (StringRealFilter5) productIdFilter;
        Assert.assertEquals(Field.PRODUCTID, stringRealProductIdFilter.getField());
        Assert.assertEquals(Operator.EQ, stringRealProductIdFilter.getOperator());
        Assert.assertEquals("Acme-1234", stringRealProductIdFilter.getValues().get(0));

        // Make sure the nested OR looks right
        QueryFilter5 orFilter = andFilter.getValues().get(2);
        Assert.assertTrue(orFilter instanceof LogicalFilter5);
        LogicalFilter5 realOrFilter = (LogicalFilter5) orFilter;
        Assert.assertEquals(Operator.OR, realOrFilter.getOperator());
        Assert.assertEquals(2, realOrFilter.getValues().size());

        // Make sure nested AND looks right
        QueryFilter5 nestedAndFilter = realOrFilter.getValues().get(1);
        Assert.assertTrue(nestedAndFilter instanceof LogicalFilter5);
        Assert.assertEquals(Operator.AND, nestedAndFilter.getOperator());
        Assert.assertEquals(3, nestedAndFilter.getValues().size());


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

