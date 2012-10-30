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
        List array = (List) fiddle.get( "array" );
        array.add( "c" );
        array.set( 1, 3 );
        Map<String,Object> subMap = (Map<String,Object>) fiddle.get( "map" );
        subMap.put("c", "c");
        subMap.put("b", 3 );

        // Verify we made the modifications we wanted to.
        Object expectedModified = JsonUtils.jsonToObject( DeepCopyTest.class.getResourceAsStream( "/json/deepcopy/modifed.json" ) );
        JoltTestUtil.runDiffy( "Verify post deepcopy object looks correct.", expectedModified, fiddle );

        // Verify that the input to the copy was unmodified
        Object unmodified = JsonUtils.jsonToObject( DeepCopyTest.class.getResourceAsStream( "/json/deepcopy/original.json" ) );
        JoltTestUtil.runDiffy( "Verify deepcopy input is unmodifed", unmodified, input );
    }
}
