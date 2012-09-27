package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class PutItAllTogetherTest {

    @DataProvider
    public Object[][] shiftrTestCases() throws IOException {
        return new Object[][] {
            {
                "Simple * and Reference",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&1(2).&.value\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"bar\" } }"),
                JsonUtils.jsonToMap("{ \"AAA\" : { \"rating-BBB\" : { \"value\" : \"bar\" } } }")
            },
            {
                "Shift to two places",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : [ \"&1(2).&.value\", \"foo\"] } }"),
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
                "Complex array output",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"tuna[&1(1)].marlin[&1(2)].&(1)\" } }"),
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

        JoltTestUtil.runDiffy(expected, actual, testName);
    }


    public void shiftrKeyOrderTest() throws IOException {
        JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&1(2).&.value\" } }");

    }
}
