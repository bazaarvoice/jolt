package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.shiftr.Path.*;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutInOutputTest {

    @DataProvider
    public Object[][] putInOutputTestCases() throws Exception {
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

    @Test(dataProvider = "putInOutputTestCases")
    public void PutInOutputTest(List<String> outputs, List<String> outputPath, Map<String, Object> expected) throws Exception
    {
        Map<String, Object> actual = new HashMap<String, Object>();

        for ( String output : outputs ) {
            Key.putInOutput(output, new StringPath( outputPath ), actual);
        }

        JoltTestUtil.runDiffy( expected, actual, "failed");
    }
}
