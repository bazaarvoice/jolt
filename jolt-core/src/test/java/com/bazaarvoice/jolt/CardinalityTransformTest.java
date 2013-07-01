/*
 * Copyright 2013 Bazaarvoice, Inc.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

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
import java.util.Map;

public class CardinalityTransformTest {

    @DataProvider
    public Object[][] getTestCaseUnits() {
        return new Object[][] {
                {"oneLiteralTestData"},
                {"manyLiteralTestData"},
                {"starTestData"},
                {"atTestData"}
        };
    }

    @Test (dataProvider = "getTestCaseUnits")
    public void runTestUnits(String testCaseName) throws IOException {

        String testPath = "/json/cardinality/" + testCaseName;
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( CardinalityTransformTest.class.getResourceAsStream( testPath + ".json" ) );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        CardinalityTransform cardinalityTransform = new CardinalityTransform( spec );
        Object actual = cardinalityTransform.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

    @Test(expectedExceptions=SpecException.class)
    public void testSpecExceptions() throws IOException {
        String testPath = "/json/cardinality/failCardinalityType";
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( CardinalityTransformTest.class.getResourceAsStream( testPath + ".json" ) );

        Object spec = testUnit.get( "spec" );

        // Should throw exception
        new CardinalityTransform( spec );
    }
}
