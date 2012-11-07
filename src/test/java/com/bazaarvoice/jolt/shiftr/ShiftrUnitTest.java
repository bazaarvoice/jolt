package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Shiftr;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShiftrUnitTest {

    @DataProvider
    public Object[][] shiftrTestCases() throws IOException {
        return new Object[][] {
            {
                "Simple * and Reference",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).&.value\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"bar\" } }"),
                JsonUtils.jsonToMap("{ \"AAA\" : { \"rating-BBB\" : { \"value\" : \"bar\" } } }")
            },
            {
                "Shift to two places",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : [ \"&(1,2).&.value\", \"foo\"] } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"bar\" } }"),
                JsonUtils.jsonToMap("{ \"foo\" : \"bar\", \"AAA\" : { \"rating-BBB\" : { \"value\" : \"bar\" } } }")
            },
            {
                "Or",
                JsonUtils.jsonToMap("{ \"tuna|marlin\" : \"&-output\" }"),
                JsonUtils.jsonToMap("{ \"tuna\" : \"snapper\" }"),
                JsonUtils.jsonToMap("{ \"tuna-output\" : \"snapper\" }")
            },
            {
                "KeyRef",
                JsonUtils.jsonToMap("{ \"rating-*\" : { \"&(0,1)\" : { \"match\" : \"&\" } } }"),
                JsonUtils.jsonToMap("{ \"rating-a\" : { \"a\" : { \"match\": \"a-match\" }, \"random\" : { \"match\" : \"noise\" } }," +
                        "              \"rating-c\" : { \"c\" : { \"match\": \"c-match\" }, \"random\" : { \"match\" : \"noise\" } } }"),
                JsonUtils.jsonToMap("{ \"match\" : [ \"a-match\", \"c-match\" ] }")
            },
            {
                "Complex array output",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"tuna[&(1,1)].marlin[&(1,2)].&(0,1)\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-2-marlin-3\" : { \"rating-BBB\" : \"bar\" }," +
                                      "\"tuna-1-marlin-0\" : { \"rating-AAA\" : \"mahi\" } }"),
                JsonUtils.jsonToMap("{ \"tuna\" : [ null, " +
                        "                           { \"marlin\" : [ { \"AAA\" : \"mahi\" } ] }, " +
                        "                           { \"marlin\" : [ null, null, null, { \"BBB\" : \"bar\" } ] } " +
                        "                         ] " +
                        "            }")
            }
        };
    }

    @Test(dataProvider = "shiftrTestCases")
    public void shiftrUnitTest(String testName, Map<String, Object> spec, Map<String, Object> data, Map<String, Object> expected) throws Exception {

        Shiftr shiftr = new Shiftr();
        Object actual = shiftr.xform( data, spec );

        JoltTestUtil.runDiffy( testName, expected, actual );
    }


    @DataProvider
    public Object[][] failureTestCases() throws IOException {
        return new Object[][] {
                {
                        "Bad @",
                        JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-@\" : \"&(1,2).&.value\" } }" ),
                },
                {
                        "RHS @",
                        JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).@.value\" } }" ),
                },
                {
                        "RHS *",
                        JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).*.value\" } }" ),
                },
                {
                        "RHS $",
                        JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).$.value\" } }" ),
                },
                {
                        "Two Arrays",
                        JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : [ \"&(1,2).photos[&(0,1)]-subArray[&(1,2)].value\", \"foo\"] } }"),
                },
                {
                        "Can't mix * and & in the same key",
                        JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-&(1,2)-*\" : [ \"&(1,2).value\", \"foo\"] } }"),
                }
        };
    }

    @Test(dataProvider = "failureTestCases")
    public void failureUnitTest(String testName, Map<String, Object> spec) throws Exception {

        Shiftr shiftr = new Shiftr();
        try {
            Object actual = shiftr.xform( new HashMap(), spec );
            AssertJUnit.fail( "TestCase : " + testName + " expected illegal argument exception" );
        }
        catch (IllegalArgumentException iae) {
        }
        catch( Exception e ){
            AssertJUnit.fail("TestCase : " + testName + " expected illegal argument exception, but got something else");
        }
    }
}
