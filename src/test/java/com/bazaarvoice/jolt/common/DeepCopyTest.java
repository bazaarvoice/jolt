package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class DeepCopyTest {

    @Test
    public void deepCopyTest() throws Exception {

        Object input = JsonUtils.jsonToObject( DeepCopyTest.class.getResourceAsStream( "/json/deepcopy/original.json" ) );

        // The test is to make a deep copy, then manipulate the copy, and verify that the original did not change  ;)
        // copy and fiddle
        Map<String, Object> fiddle = (Map<String, Object>) DeepCopy.simpleDeepCopy( input );
        List stringArray = (List) fiddle.get( "array" );
        stringArray.add( "c" );
        stringArray.set( 1, 3 );
        Map<String,Object> subMap = (Map<String,Object>) fiddle.get( "map" );
        subMap.put("c", "c");
        subMap.put("b", 3 );

        Object unmodified = JsonUtils.jsonToObject( DeepCopyTest.class.getResourceAsStream( "/json/deepcopy/original.json" ) );
        JoltTestUtil.runDiffy( "Failed deep copy test.", unmodified, input );


        Object expectedModified = JsonUtils.jsonToObject( DeepCopyTest.class.getResourceAsStream( "/json/deepcopy/modifed.json" ) );
        JoltTestUtil.runDiffy( "Failed deep copy test.", expectedModified, fiddle );
    }
}
