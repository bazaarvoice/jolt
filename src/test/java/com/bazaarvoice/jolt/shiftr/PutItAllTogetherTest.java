package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.Diffy;

import com.bazaarvoice.jolt.JsonUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Map;

public class PutItAllTogetherTest {

    @Test
    public void putItAllTogetherTest() throws Exception {

        Map<String,Object> specJson = JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&1(2).&.value\" } }");
        Map<String,Object> data     = JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"tuna\" } }");
        Map<String,Object> expected = JsonUtils.jsonToMap("{ \"AAA\" : { \"rating-BBB\" : { \"value\" : \"tuna\" } } }");

        Shiftr shiftr = new Shiftr();
        Object actual = shiftr.xform( data, specJson );

        Diffy diffy = new Diffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "failed case.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }
}
