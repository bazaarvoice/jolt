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

import com.bazaarvoice.jolt.exception.TypeConversionException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class ConvertrTest {

    @DataProvider
    public Object[][] getDiffyTestCases() {
        return new Object[][] {
                {"arrayMismatch"},
                {"conversionTest"},
                {"defaultNulls"},
                {"invalidConversion"},
                {"nestedArrays"}
        };
    }

    @DataProvider
    public Object[][] getBadTypeTestCases() {
        return new Object[][] {
                {"badTypeTest1"},
                {"badTypeTest2"},
                {"badTypeTest3"}
        };
    }

    @Test(dataProvider = "getDiffyTestCases" )
    public void runDiffyTests( String testCaseName ) throws IOException {

        String testPath = "/json/convertr/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath + ".json" );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Convertr convertr = new Convertr(spec);
        Object actual = convertr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

    @Test(expectedExceptions = TypeConversionException.class, dataProvider = "getBadTypeTestCases")
    public void throwExceptionOnBadType(String testCaseName) throws IOException {
        String testPath = "/json/convertr/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath + ".json" );

        Object input = testUnit.get("input");
        Object spec = testUnit.get("spec");

        Convertr convertr = new Convertr(spec);
        convertr.transform(input);
    }
}
