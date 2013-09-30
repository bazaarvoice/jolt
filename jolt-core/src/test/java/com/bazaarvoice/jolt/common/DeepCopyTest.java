/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class DeepCopyTest {

    @Test
    public void deepCopyTest() throws Exception {

        Object input = JsonUtils.classpathToObject( "/json/deepcopy/original.json" );

        Map<String, Object> fiddle = (Map<String, Object>) DeepCopy.simpleDeepCopy( input );

        JoltTestUtil.runDiffy( "Verify that the DeepCopy did in fact make a copy.", input, fiddle );

        // The test is to make a deep copy, then manipulate the copy, and verify that the original did not change  ;)
        // copy and fiddle
        List array = (List) fiddle.get( "array" );
        array.add( "c" );
        array.set( 1, 3 );
        Map<String,Object> subMap = (Map<String,Object>) fiddle.get( "map" );
        subMap.put("c", "c");
        subMap.put("b", 3 );

        // Verify that the input to the copy was unmodified
        Object unmodified = JsonUtils.classpathToObject( "/json/deepcopy/original.json" );
        JoltTestUtil.runDiffy( "Verify that the deepcopy was actually deep / input is unmodified", unmodified, input );

        // Verify we made the modifications we wanted to.
        Object expectedModified = JsonUtils.classpathToObject( "/json/deepcopy/modifed.json" );
        JoltTestUtil.runDiffy( "Verify fiddled post deepcopy object looks correct / was modifed.", expectedModified, fiddle );
    }
}
