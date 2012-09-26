package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class PutItAllTogetherTest {

    @DataProvider
    public Object[][] putInOutputTestCases() throws Exception {
        return new Object[][] {
            {
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&1(2).&.value\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"tuna\" } }"),
                JsonUtils.jsonToMap("{ \"AAA\" : { \"rating-BBB\" : { \"value\" : \"tuna\" } } }")
            },
            {
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&1(2).&.value\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"tuna\" } }"),
                JsonUtils.jsonToMap("{ \"AAA\" : { \"rating-BBB\" : { \"value\" : \"tuna\" } } }")
            }
        };
    }

    @Test(dataProvider = "putInOutputTestCases")
    public void putItAllTogetherTest(Map<String,Object> spec, Map<String,Object> data, Map<String,Object> expected ) throws Exception {

        Shiftr shiftr = new Shiftr();
        Object actual = shiftr.xform( data, spec );

        JoltTestUtil.runDiffy(expected, actual, "failed");
    }
}
