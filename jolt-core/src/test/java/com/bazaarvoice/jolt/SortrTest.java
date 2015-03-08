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

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SortrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            { "simple" }
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName) throws IOException {

        if ("".equals( testCaseName )) {
            return;
        }

        String testPath = "/json/sortr/"+testCaseName;
        Map<String, Object> input = JsonUtils.classpathToMap(testPath + "/input.json");
        Map<String, Object> expected = JsonUtils.classpathToMap( testPath + "/output.json" );

        Sortr sortr = new Sortr();
        Map<String, Object> actual = (Map<String, Object>) sortr.transform( input );

        JoltTestUtil.runDiffy( "Make sure it is still the same object : " + testPath, expected, actual );

        // Make sure the sort actually worked.
        String orderErrorMessage = verifyOrder( actual, expected );
        Assert.assertNull( orderErrorMessage, orderErrorMessage );
    }

    public static String verifyOrder( Object actual, Object expected ) {
        if ( actual instanceof Map && expected instanceof Map ) {
            return verifyMapOrder( (Map<String, Object>) actual, (Map<String, Object>) expected );
        } else if ( actual instanceof List && expected instanceof List ) {
            return verifyListOrder( (List<Object>) actual, (List<Object>) expected ) ;
        } else {
            return null;
        }
    }

    private static String verifyMapOrder( Map<String,Object> actual, Map<String,Object> expected ) {

        Iterator<String> actualIter = actual.keySet().iterator();
        Iterator<String> expectedIter = expected.keySet().iterator();

        for( int index = 0; index < actual.size(); index++ ) {
            String actualKey = actualIter.next();
            String expectedKey = expectedIter.next();

            if ( ! StringUtils.equals( actualKey, expectedKey ) ) {
                return "Found out of order keys '" + actualKey + "' and '" + expectedKey + "'";
            }

            String result = verifyOrder( actual.get( actualKey), expected.get(expectedKey) );
            if ( result != null ) {
                return result;
            }
        }

        return null; // success
    }

    private static String verifyListOrder( List<Object> actual, List<Object> expected ) {

        for( int index = 0; index < actual.size(); index++ ) {
            String result = verifyOrder( actual.get( index ), expected.get(index) );
            if ( result != null ) {
                return result;
            }
        }

        return null; // success
    }

    @Test
    public void testDoesNotBlowUpOnUnmodifiableArray() {
        List<Object> hasNan = new ArrayList<>();
        hasNan.add( 1 );
        hasNan.add( Double.NaN );
        hasNan.add( 2 );

        Map<String,Object> map = new HashMap<>();
        map.put("a", "shouldBeFirst");
        map.put("hasNan", Collections.unmodifiableList( hasNan ) );

        try {
            Sortr.sortJson( map );
        }
        catch( UnsupportedOperationException uoe ) {
            Assert.fail( "Sort threw a UnsupportedOperationException" );
        }
    }
}
