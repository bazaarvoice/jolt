package com.bazaarvoice.jolt;

import org.apache.commons.lang.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SortrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"simple" }
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName) throws IOException {

        if ("".equals( testCaseName )) {
            return;
        }

        String testPath = "/json/sortr/"+testCaseName;
        Map<String, Object> input = (Map<String, Object>) JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/input.json") );
        Map<String, Object> expected = (Map<String, Object>) JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/output.json" ) );

        Sortr sortr = new Sortr();
        Map<String, Object> actual = (Map<String, Object>) sortr.transform( input );

        JoltTestUtil.runDiffy( "Make sure it is still the same object : " + testPath, expected, actual );

        // Make sure the sort actually worked.
        String orderErrorMessage = verifyOrder( actual, expected );
        AssertJUnit.assertNull( orderErrorMessage, orderErrorMessage );
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
}
