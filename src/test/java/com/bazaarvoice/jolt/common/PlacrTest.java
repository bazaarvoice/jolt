package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.shiftr.Path.*;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlacrTest {

    @Test
    public void arrayOutputKeyTest() {

        Pattern pattern = Placr.arrayKeyPattern;

        {
            Matcher matcher = pattern.matcher( "photos[2]" );

            AssertJUnit.assertTrue( matcher.find() );
            AssertJUnit.assertEquals( "photos", matcher.group(1) );
            AssertJUnit.assertEquals( "2", matcher.group(2) );
        }
        {
            Matcher matcher = pattern.matcher( "photos" );

            AssertJUnit.assertFalse( matcher.find() );
        }
    }

    @DataProvider
    public Object[][] putInOutputTestCases() throws Exception {
        return new Object[][] {
            {
                "simple place",
                Arrays.asList( "tuna" ),
                Arrays.asList( "a", "b" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : \"tuna\" } }" )
            },
            {
                "simple array place",
                Arrays.asList( "tuna" ),
                Arrays.asList( "a", "b[1]" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, \"tuna\" ] } }" )
            },
            {
                "nested array place",
                Arrays.asList( "tuna" ),
                Arrays.asList( "a", "b[1]", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, { \"c\" : \"tuna\" } ] } }" )
            },
            {
                "simple place into output array",
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "a", "b" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ \"tuna\", \"marlin\" ] } }" )
            },
            {
                "simple array place with nested output array",
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "a", "b[1]" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, [ \"tuna\", \"marlin\" ] ] } }" )
            },
            {
                "nested array place with nested ouptut array",
                Arrays.asList( "tuna", "marlin" ),
                Arrays.asList( "a", "b[1]", "c" ),
                JsonUtils.jsonToMap( "{ \"a\" : { \"b\" : [ null, { \"c\" : [ \"tuna\", \"marlin\"] } ] } }" )
            }
        };
    }

    @Test(dataProvider = "putInOutputTestCases")
    public void PutInOutputTest(String testCaseName, List<String> outputs, List<String> outputPath, Map<String, Object> expected) throws Exception
    {
        Map<String, Object> actual = new HashMap<String, Object>();

        for ( String output : outputs ) {
            Placr.placeInOutput( output, new StringPath( outputPath ), actual );
        }

        JoltTestUtil.runDiffy( testCaseName, expected, actual.get( Placr.OUTPUT_PREFIX_KEY) );
    }
}
