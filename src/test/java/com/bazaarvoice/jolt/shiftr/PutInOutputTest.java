package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutInOutputTest {

    @DataProvider
    public Object[][] getTestCaseNames() throws Exception {
        return new Object[][] {
            {
                Arrays.asList( "tuna" ),
                Arrays.asList( "a", "b" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"tuna\" } }" )
            },
            {
                Arrays.asList( "tuna" ),
                Arrays.asList( "a", "b[1]" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, \"tuna\" ] } }" )
            },
            {
                Arrays.asList( "tuna" ),
                Arrays.asList( "a", "b[1]", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, { \"c\" : \"tuna\" } ] } }" )
            },
            {
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "a", "b" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ \"tuna\", \"marlin\" ] } }" )
            },
            {
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "a", "b[1]" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, [ \"tuna\", \"marlin\" ] ] } }" )
            },
            {
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "a", "b[1]", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, { \"c\" : [ \"tuna\", \"marlin\"] } ] } }" )
            }
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void PutInOutputTest(List<String> outputs, List<String> outputPath, Map<String, Object> expected) throws Exception
    {
        Map<String, Object> actual = new HashMap<String, Object>();

        for ( String output : outputs ) {
            Key.putInOutput( actual, output, new Path<String>( outputPath ) );
        }

        Diffy diffy = new Diffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "failed case.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }
}
