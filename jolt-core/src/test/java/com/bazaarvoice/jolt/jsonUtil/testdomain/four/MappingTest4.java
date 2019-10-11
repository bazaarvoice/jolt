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

import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtil;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.jsonUtil.testdomain.QueryParam;
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

public class MappingTest4 {

    private Diffy diffy = new Diffy();

    public static class QueryFilter4Deserializer extends JsonDeserializer<QueryFilter4> {

        /**
         * Demonstrates how to do recursive polymorphic JSON deserialization in Jackson 2.2.
         *
         * Aka specify a Deserializer and "catch" some input, determine what type of Class it
         *  should be parsed too, and then reuse the Jackson infrastructure to recursively do so.
         */
        @Override
        public QueryFilter4 deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException {

            ObjectNode root = jp.readValueAsTree();

            // pass in our objectCodec so that the subJsonParser knows about our configured Modules and Annotations
            JsonParser subJsonParser = root.traverse( jp.getCodec() );

            // Check if it is a "RealFilter"
            JsonNode valueParam = root.get("value");

            if ( valueParam == null ) {
                 return subJsonParser.readValueAs( LogicalFilter4.class );
            }
            if ( valueParam.isBoolean() ) {
                return subJsonParser.readValueAs( BooleanRealFilter4.class );
            }
            else if ( valueParam.isTextual() ) {
                return subJsonParser.readValueAs( StringRealFilter4.class );
            }
            else if ( valueParam.isIntegralNumber() ) {
                return subJsonParser.readValueAs( IntegerRealFilter4.class );
            }
            else {
                throw new RuntimeException("Unknown type");
            }
        }
    }

    @Test
    public void testPolymorphicJacksonSerializationAndDeserialization()
    {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule testModule = new SimpleModule("testModule", new Version(1, 0, 0, null, null, null))
                .addDeserializer( QueryFilter4.class, new QueryFilter4Deserializer() );

        mapper.registerModule(testModule);

        // Verifying that we can pass in a custom Mapper and create a new JsonUtil
        JsonUtil jsonUtil = JsonUtils.customJsonUtil( mapper );

        String testFixture = "/jsonUtils/testdomain/four/queryFilter-realAndLogical4.json";

        // TEST JsonUtil and our deserialization logic
        QueryFilter4 queryFilter = jsonUtil.classpathToType( testFixture, new TypeReference<QueryFilter4>() {} );

        // Make sure the hydrated QFilter looks right
        Assert.assertTrue( queryFilter instanceof LogicalFilter4);
        Assert.assertEquals( QueryParam.AND, queryFilter.getQueryParam() );
        Assert.assertTrue( queryFilter.isLogical() );
        Assert.assertEquals( 3, queryFilter.getFilters().size() );
        Assert.assertNotNull( queryFilter.getFilters().get( QueryParam.OR ) );

        // Make sure one of the top level RealFilters looks right
        QueryFilter4 productIdFilter = queryFilter.getFilters().get( QueryParam.PRODUCTID );
        Assert.assertTrue( productIdFilter.isReal() );
        Assert.assertTrue( productIdFilter instanceof StringRealFilter4);
        StringRealFilter4 stringRealProductIdFilter = (StringRealFilter4) productIdFilter;
        Assert.assertEquals( QueryParam.PRODUCTID, stringRealProductIdFilter.getQueryParam() );
        Assert.assertEquals( "Acme-1234", stringRealProductIdFilter.getValue() );

        // Make sure the nested OR looks right
        QueryFilter4 orFilter = queryFilter.getFilters().get( QueryParam.OR );
        Assert.assertTrue( orFilter.isLogical() );
        Assert.assertEquals( QueryParam.OR, orFilter.getQueryParam() );
        Assert.assertEquals( 2, orFilter.getFilters().size() );

        // Make sure nested AND looks right
        QueryFilter4 nestedAndFilter = orFilter.getFilters().get( QueryParam.AND );
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
