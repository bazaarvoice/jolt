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
package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class DefaultrTest {

    @DataProvider
    public Object[][] getDiffyTestCases() {
        return new Object[][] {
            {"arrayMismatch1"},
            {"arrayMismatch2"},
            {"defaultNulls"},
            {"expansionOnly"},
            {"firstSample"},
            {"identity"},
            {"nestedArrays1"},
            {"nestedArrays2"},
            {"orOrdering"},
            {"photosArray"},
            {"starsOfStars"},
            {"topLevelIsArray"},
        };
    }

    @Test(dataProvider = "getDiffyTestCases" )
    public void runDiffyTests( String testCaseName ) throws IOException {

        String testPath = "/json/defaultr/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath + ".json" );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Defaultr defaultr = new Defaultr(spec);
        Object actual = defaultr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

    @Test
    public void deepCopyTest() throws IOException {
        Map<String, Object> testUnit = JsonUtils.classpathToMap( "/json/defaultr/__deepCopyTest.json" );

        Object spec = testUnit.get( "spec" );

        Defaultr defaultr = new Defaultr(spec);
        {
            Object input = testUnit.get( "input" );
            Map<String, Object> fiddle = (Map<String, Object>) defaultr.transform( input );

            List array = (List) fiddle.get( "array" );
            array.add("a");

            Map<String,Object> subMap = (Map<String,Object>) fiddle.get( "map" );
            subMap.put("c", "c");
        }
        {
            Map<String, Object> testUnit2 = JsonUtils.classpathToMap( "/json/defaultr/__deepCopyTest.json" );

            Object input = testUnit2.get( "input" );
            Object expected = testUnit2.get( "expected" );

            Object actual = defaultr.transform( input );
            JoltTestUtil.runDiffy( "Same spec deepcopy fail.", expected, actual );
        }
    }

    @Test(expectedExceptions = SpecException.class)
    public void throwExceptionOnBadSpec() throws IOException {
        Object spec = JsonUtils.jsonToMap( "{ \"tuna*\": \"marlin\" }" );
        new Defaultr( spec );
    }
}
