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
package com.bazaarvoice.jolt.jsonUtil.testdomain.three;

import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtil;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.jsonUtil.testdomain.QueryFilter;
import com.bazaarvoice.jolt.jsonUtil.testdomain.QueryParam;
import com.bazaarvoice.jolt.jsonUtil.testdomain.RealFilter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class MappingTest3 {

    private Diffy diffy = new Diffy();

    public static class QueryFilterDeserializer extends JsonDeserializer<QueryFilter> {

        /**
         * Demonstrates how to do recursive polymorphic JSON deserialization in Jackson 2.2.
         *
         * Aka specify a Deserializer and "catch" some input, determine what type of Class it
         *  should be parsed too, and then reuse the Jackson infrastructure to recursively do so.
         */
        @Override
        public QueryFilter deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {

            ObjectNode root = jp.readValueAsTree();

            // pass in our objectCodec so that the subJsonParser knows about our configured Modules and Annotations
            JsonParser subJsonParser = root.traverse( jp.getCodec() );

            // Check if it is a "RealFilter"
            JsonNode queryParam = root.get("queryParam");
            if ( queryParam != null && queryParam.isValueNode() ) {
                return subJsonParser.readValueAs( RealFilter.class );
            }
            else {
                return subJsonParser.readValueAs( LogicalFilter3.class );
            }
        }
    }

    @Test
    public void testPolymorphicJacksonSerializationAndDeserialization()
    {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule testModule = new SimpleModule("testModule", new Version(1, 0, 0, null, null, null))
                .addDeserializer( QueryFilter.class, new QueryFilterDeserializer() );

        mapper.registerModule(testModule);

        // Verifying that we can pass in a custom Mapper and create a new JsonUtil
        JsonUtil jsonUtil = JsonUtils.customJsonUtil( mapper );

        String testFixture = "/jsonUtils/testdomain/two/queryFilter-realAndLogical2.json";

        // TEST JsonUtil and our deserialization logic
        QueryFilter queryFilter = jsonUtil.classpathToType( testFixture, new TypeReference<QueryFilter>() {} );

        // Make sure the hydrated QFilter looks right
        Assert.assertTrue( queryFilter instanceof LogicalFilter3 );
        Assert.assertEquals( QueryParam.AND, queryFilter.getQueryParam() );
        Assert.assertTrue( queryFilter.isLogical() );
        Assert.assertEquals( 3, queryFilter.getFilters().size() );
        Assert.assertNotNull( queryFilter.getFilters().get( QueryParam.OR ) );

        // Make sure one of the top level RealFilters looks right
        QueryFilter productIdFilter = queryFilter.getFilters().get( QueryParam.PRODUCTID );
        Assert.assertTrue( productIdFilter.isReal() );
        Assert.assertEquals( QueryParam.PRODUCTID, productIdFilter.getQueryParam() );
        Assert.assertEquals( "Acme-1234", productIdFilter.getValue() );

        // Make sure the nested OR looks right
        QueryFilter orFilter = queryFilter.getFilters().get( QueryParam.OR );
        Assert.assertTrue( orFilter.isLogical() );
        Assert.assertEquals( QueryParam.OR, orFilter.getQueryParam() );
        Assert.assertEquals( 2, orFilter.getFilters().size() );

        // Make sure nested AND looks right
        QueryFilter nestedAndFilter = orFilter.getFilters().get( QueryParam.AND );
        Assert.assertTrue( nestedAndFilter.isLogical() );
        Assert.assertEquals( QueryParam.AND, nestedAndFilter.getQueryParam() );
        Assert.assertEquals( 2, nestedAndFilter.getFilters().size() );


        // SERIALIZE TO STRING to test serialization logic
        String unitTestString = jsonUtil.toJsonString( queryFilter );

        // LOAD and Diffy the plain vanilla JSON versions of the documents
        Map<String, Object> actual   = JsonUtils.jsonToMap( unitTestString );
        Map<String, Object> expected = JsonUtils.classpathToMap( testFixture );

        // Diffy the vanilla versions
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }
}
