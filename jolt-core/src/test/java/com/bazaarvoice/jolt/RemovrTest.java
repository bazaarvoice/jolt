package com.bazaarvoice.jolt;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class RemovrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"firstSample"}
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName) throws IOException {

        String testPath = "/json/removr/" + testCaseName;
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( Removr.class.getResourceAsStream( testPath + ".json" ) );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Removr removr = new Removr( spec );
        Object actual = removr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
