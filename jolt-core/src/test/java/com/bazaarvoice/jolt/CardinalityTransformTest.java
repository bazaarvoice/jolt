package com.bazaarvoice.jolt;

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
                {"atTestData"}
        };
    }

    @Test (dataProvider = "getTestCaseUnits")
    public void runTestUnits(String testCaseName) throws IOException {

        String testPath = "/json/cardinality/" + testCaseName;
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( CardinalityTransform.class.getResourceAsStream( testPath + ".json" ) );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        CardinalityTransform cardinalityTransform = new CardinalityTransform( spec );
        Object actual = cardinalityTransform.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
