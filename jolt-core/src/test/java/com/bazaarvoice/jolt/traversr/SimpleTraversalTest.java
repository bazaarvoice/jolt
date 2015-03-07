/*
 * Copyright 2013 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt.traversr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleTraversalTest {

    @DataProvider
    public Object[][] inAndOutTestCases() throws Exception {
        return new Object[][] {
            {
                "Simple Map Test",
                SimpleTraversal.newTraversal( "a.b" ),
                JsonUtils.jsonToMap( "{ \"a\" : null }" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"tuna\" } }" ),
                "tuna"
            },
            {
                "Simple explicit array test",
                SimpleTraversal.newTraversal( "a.[1].b" ),
                JsonUtils.jsonToMap( "{ \"a\" : null }" ),
                JsonUtils.jsonToMap( "{ \"a\" : [ null, { \"b\" : \"tuna\" } ] }" ),
                "tuna"
            },
            {
                "Leading Array test",
                SimpleTraversal.newTraversal( "[0].a" ),
                JsonUtils.jsonToObject( "[ ]" ),
                JsonUtils.jsonToObject( "[ { \"a\" : \"b\" } ]" ),
                "b"
            },
            {
                "Auto expand array test",
                SimpleTraversal.newTraversal( "a.[].b" ),
                JsonUtils.jsonToMap( "{ \"a\" : null }" ),
                JsonUtils.jsonToMap( "{ \"a\" : [ { \"b\" : null } ] }" ),
                null
            }
        };
    }

    @Test( dataProvider = "inAndOutTestCases")
    public void getTests( String testDescription, SimpleTraversal simpleTraversal, Object ignoredForTest, Object input, String expected ) throws IOException {

        Object original = JsonUtils.cloneJson( input );
        Object tree = JsonUtils.cloneJson( input );

        Object actual = simpleTraversal.get( tree );

        Assert.assertEquals( expected, actual );
        JoltTestUtil.runDiffy( "Get should not have modified the input", original, tree );
    }

    @Test( dataProvider = "inAndOutTestCases")
    public void setTests( String testDescription, SimpleTraversal simpleTraversal, Object start, Object expected, String toSet ) {

        Object actual = JsonUtils.cloneJson( start );

        Assert.assertEquals( toSet, simpleTraversal.set( actual, toSet ) ); // set should be successful

        Assert.assertEquals( expected, actual );
    }

    @Test
    public void testAutoArray() throws IOException
    {
        SimpleTraversal<String> traversal = SimpleTraversal.newTraversal( "a.[].b" );

        Object expected = JsonUtils.jsonToMap( "{ \"a\" : [ { \"b\" : \"one\" }, { \"b\" : \"two\" } ] }" );

        Object actual = new HashMap();

        Assert.assertNull( traversal.get( actual ) );
        Assert.assertEquals( 0, ((HashMap) actual).size() ); // get didn't add anything

        // Add two things and validate the Auto Expand array
        Assert.assertEquals( "one", traversal.set( actual, "one" ) );
        Assert.assertEquals( "two", traversal.set( actual, "two" ) );

        JoltTestUtil.runDiffy( expected, actual );
    }

    @Test
    public void testOverwrite() throws IOException
    {
        SimpleTraversal<String> traversal = SimpleTraversal.newTraversal( "a.b" );

        Object actual = JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"tuna\" } }" );
        Object expectedOne = JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"one\" } }" );
        Object expectedTwo = JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"two\" } }" );

        Assert.assertEquals( "tuna", traversal.get( actual ) );

        // Set twice and verify that the sets did in fact overwrite
        Assert.assertEquals( "one", traversal.set( actual, "one" ) );
        JoltTestUtil.runDiffy( expectedOne, actual );

        Assert.assertEquals( "two", traversal.set( actual, "two" ) );
        JoltTestUtil.runDiffy( expectedTwo, actual );
    }

    @DataProvider
    public Object[][] removeTestCases() throws Exception {
        return new Object[][] {
            {
                "Inception Map Test",
                SimpleTraversal.newTraversal( "__queryContext" ),
                JsonUtils.javason( "{ 'Id' : '1234', '__queryContext' : { 'catalogLin' : [ 'a', 'b' ] } }" ),
                JsonUtils.javason( "{ 'Id' : '1234' }" ),
                JsonUtils.javason( "{ 'catalogLin' : [ 'a', 'b' ] }" )
            },
            {
                "List Test",
                SimpleTraversal.newTraversal( "a.list.[1]" ),
                JsonUtils.javason( "{ 'a' : { 'list' : [ 'a', 'b', 'c' ] } }" ),
                JsonUtils.javason( "{ 'a' : { 'list' : [ 'a', 'c' ] } }" ),
                "b"
            },
            {
                "Map leave empty Map",
                SimpleTraversal.newTraversal( "a.list" ),
                JsonUtils.javason( "{ 'a' : { 'list' : [ 'a', 'b', 'c' ] } }" ),
                JsonUtils.javason( "{ 'a' : { } }" ),
                Arrays.asList( "a","b","c" )
            },
            {
                "Map leave empty List",
                SimpleTraversal.newTraversal( "a.list.[0]" ),
                JsonUtils.javason( "{ 'a' : { 'list' : [ 'a' ] } }" ),
                JsonUtils.javason( "{ 'a' : { 'list' : [ ] } }" ),
                "a"
            }
        };
    }

    @Test( dataProvider = "removeTestCases")
    public void removeTests( String testDescription, SimpleTraversal simpleTraversal,
                             Object start, Object expectedLeft, Object expectedReturn )
                             throws Exception
    {

        Object actualRemove = simpleTraversal.remove( start );
        JoltTestUtil.runDiffy( testDescription, expectedReturn, actualRemove );

        JoltTestUtil.runDiffy( testDescription, expectedLeft, start );
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void exceptionTestListIsMap() throws Exception
    {
        Object tree = JsonUtils.javason( "{ 'Id' : '1234', '__queryContext' : { 'catalogLin' : [ 'a', 'b' ] } }" );

        SimpleTraversal<List> trav = SimpleTraversal.newTraversal( "__queryContext" );
        // barfs here, needs the 'List list =' part to trigger it
        @SuppressWarnings( "unused" )
        List list = trav.get( tree );
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void exceptionTestMapIsList() throws Exception
    {
        Object tree = JsonUtils.javason( "{ 'Id' : '1234', '__queryContext' : { 'catalogLin' : [ 'a', 'b' ] } }" );

        SimpleTraversal<Map> trav = SimpleTraversal.newTraversal( "__queryContext.catalogLin" );
        // barfs here, needs the 'Map map =' part to trigger it
        @SuppressWarnings( "unused" )
        Map map = trav.get( tree );
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void exceptionTestListIsMapErasure() throws Exception
    {
        Object tree = JsonUtils.javason( "{ 'Id' : '1234', '__queryContext' : { 'catalogLin' : [ 'a', 'b' ] } }" );

        SimpleTraversal<Map<String,Map>> trav = SimpleTraversal.newTraversal( "__queryContext" );
        // this works
        Map<String,Map> queryContext = trav.get( tree );

        // this does not
        @SuppressWarnings( "unused" )
        Map catalogLin = queryContext.get( "catalogLin" );
        Assert.fail( "Expected ClassCast Exception");
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void exceptionTestLMapIsListErasure() throws Exception
    {
        Object tree = JsonUtils.javason( "{ 'Id' : '1234', '__queryContext' : { 'catalogLin' : { 'a' : 'b' } } }" );

        SimpleTraversal<Map<String,List>> trav = SimpleTraversal.newTraversal( "__queryContext" );
        // this works
        Map<String,List> queryContext = trav.get( tree );

        // this does not
        @SuppressWarnings( "unused" )
        List catalogLin = queryContext.get( "catalogLin" );
        Assert.fail( "Expected ClassCast Exception");
    }
}
