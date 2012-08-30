package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

public class DefaultrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"firstSample"},
            {"identity"},
            {"expansionOnly"},
            {"photosArray"},
            {"arrayMismatch1"},
            {"arrayMismatch2"},
            {"starsOfStars"},
            {"topLevelIsArray"},
            {"orOrdering"}
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName)
            throws IOException {
        if ("".equals( testCaseName )) {
            return;
        }
        String testPath = "/json/defaultr/" + testCaseName;
        Object input = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + "/input.json" ) );
        Object spec = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + "/spec.json" ) );
        Object expected = JsonUtils.jsonToObject( Defaultr.class.getResourceAsStream( testPath + "/output.json" ) );

        Defaultr defaultr = new Defaultr();
        Object actual = defaultr.defaultr( spec, input );

        Diffy diffy = new ArrayDisorderDiffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail("failed case " + testPath + ".\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\nactual: " + JsonUtils.toJsonString(result.actual));
        }
        AssertJUnit.assertTrue( testPath, result.isEmpty() );
    }

    @Test
    public void throwExceptionOnNullDefaultee() throws IOException {
        Object spec = JsonUtils.jsonToMap( "{ \"tuna\": \"marlin\" }" );

        Defaultr defaultr = new Defaultr();
        try {
            defaultr.defaultr( spec, null );
            AssertJUnit.fail("expected illegal argument exception");
        }
        catch( IllegalArgumentException iae )
        { }
        catch( Exception e ){
            AssertJUnit.fail("expected illegal argument exception, but got something else");
        }
    }

    @Test
    public void throwExceptionOnBadSpec() throws IOException {
        Object spec = JsonUtils.jsonToMap( "{ \"tuna*\": \"marlin\" }" );

        Defaultr defaultr = new Defaultr();
        try {
            defaultr.defaultr( spec, new LinkedHashMap() );
            AssertJUnit.fail("expected illegal argument exception");
        }
        catch( IllegalArgumentException iae )
        { }
        catch( Exception e ){
            AssertJUnit.fail("expected illegal argument exception, but got something else");
        }
    }

    private class ArrayDisorderDiffy extends Diffy {
        protected Result diffList(List expected, List actual) {
            Result result = super.diffList( expected, actual );
            if (result.isEmpty()) {
                return result;
            }
            for (int i=expected.size()-1; i>=0; i--) {
                int idx = actual.indexOf( expected.get( i ) );
                if (idx >= 0) {
                    expected.remove( i );
                    actual.remove( idx );
                }
            }
            if (expected.isEmpty() && actual.isEmpty()) {
                return new Result();
            }
            return new Result( expected, actual );
        }
    }


}
