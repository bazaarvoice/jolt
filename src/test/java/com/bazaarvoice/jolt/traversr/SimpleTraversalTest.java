package com.bazaarvoice.jolt.traversr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

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

        AssertJUnit.assertEquals( expected, actual );
        JoltTestUtil.runDiffy( "Get should not have modified the input", original, tree );
    }

    @Test( dataProvider = "inAndOutTestCases")
    public void setTests( String testDescription, SimpleTraversal simpleTraversal, Object start, Object expected, String toSet ) {

        Object actual = JsonUtils.cloneJson( start );

        AssertJUnit.assertEquals( toSet, simpleTraversal.set( actual, toSet ) ); // set should be successful

        AssertJUnit.assertEquals( expected, actual );
    }

    @Test
    public void testAutoArray() throws IOException
    {
        SimpleTraversal<String> traversal = SimpleTraversal.newTraversal( "a.[].b" );

        Object expected = JsonUtils.jsonToMap( "{ \"a\" : [ { \"b\" : \"one\" }, { \"b\" : \"two\" } ] }" );

        Object actual = new HashMap();

        AssertJUnit.assertNull( traversal.get( actual ) );
        AssertJUnit.assertEquals( 0, ((HashMap) actual).size() ); // get didn't add anything

        // Add two things and validate the Auto Expand array
        AssertJUnit.assertEquals( "one", traversal.set( actual, "one" ) );
        AssertJUnit.assertEquals( "two", traversal.set( actual, "two" ) );

        JoltTestUtil.runDiffy( expected, actual );
    }

    @Test
    public void testOverwrite() throws IOException
    {
        SimpleTraversal<String> traversal = SimpleTraversal.newTraversal( "a.b" );

        Object actual = JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"tuna\" } }" );
        Object expectedOne = JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"one\" } }" );
        Object expectedTwo = JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"two\" } }" );

        AssertJUnit.assertEquals( "tuna", traversal.get( actual ) );

        // Add two things and validate the Auto Expand array
        AssertJUnit.assertEquals( "one", traversal.set( actual, "one" ) );
        JoltTestUtil.runDiffy( expectedOne, actual );

        AssertJUnit.assertEquals( "two", traversal.set( actual, "two" ) );
        JoltTestUtil.runDiffy( expectedTwo, actual );
    }
}
