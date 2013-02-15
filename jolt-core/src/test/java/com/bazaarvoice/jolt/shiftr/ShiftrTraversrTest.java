package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.traversr.Traversr;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShiftrTraversrTest {

    @DataProvider
    public Object[][] inAndOutTestCases() throws Exception {
        return new Object[][] {
            {
                "simple place",
                Arrays.asList( "tuna" ),
                "tuna",
                "a.b",
                Arrays.asList( "a", "b" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"tuna\" } }" )
            },
            {
                "simple explicit array place",
                Arrays.asList( "tuna" ),
                null,
                "a.b[]",
                Arrays.asList( "a", "b", "[]" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ \"tuna\" ] } }" )
            },
            {
                "simple explicit array place with sub",
                Arrays.asList( "tuna" ),
                null,
                "a.b[].c",
                Arrays.asList( "a", "b", "[]", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ { \"c\" : \"tuna\" } ] } }" )
            },
            {
                "simple array place",
                Arrays.asList( "tuna" ),
                "tuna",
                "a.b.[1]",
                Arrays.asList( "a", "b", "1" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, \"tuna\" ] } }" )
            },
            {
                "nested array place",
                Arrays.asList( "tuna" ),
                "tuna",
                "a.b[1].c",
                Arrays.asList( "a", "b", "1", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, { \"c\" : \"tuna\" } ] } }" )
            },
            {
                "simple place into write array",
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "tuna", "marlin" ),
                "a.b",
                Arrays.asList( "a", "b" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ \"tuna\", \"marlin\" ] } }" )
            },
            {
                "simple array place with nested write array",
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "tuna", "marlin" ),
                "a.b.[1]",
                Arrays.asList( "a", "b", "1" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, [ \"tuna\", \"marlin\" ] ] } }" )
            },
            {
                "nested array place with nested ouptut array",
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "tuna", "marlin" ),
                "a.b.[1].c",
                Arrays.asList( "a", "b", "1", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, { \"c\" : [ \"tuna\", \"marlin\"] } ] } }" )
            }
        };
    }

    @Test(dataProvider = "inAndOutTestCases")
    public void setTest(String testCaseName, List<String> outputs, Object notUsedInThisTest, String traversrPath, List<String> keys, Map<String, Object> expected) throws Exception
    {
        Map<String, Object> actual = new HashMap<String, Object>();

        Traversr traversr = new ShiftrTraversr( traversrPath );
        for ( String output : outputs ) {
            traversr.set( actual, keys, output );
        }

        JoltTestUtil.runDiffy( testCaseName, expected, actual );
    }

    @Test(dataProvider = "inAndOutTestCases")
    public void getTest(String testCaseName, List<String> notUsedInThisTest, Object expected, String traversrPath, List<String> keys, Map<String, Object> tree) throws Exception
    {
        Traversr traversr = new ShiftrTraversr( traversrPath  );
        Object actual = traversr.get( tree, keys);

        JoltTestUtil.runDiffy( testCaseName, expected, actual );
    }
}
