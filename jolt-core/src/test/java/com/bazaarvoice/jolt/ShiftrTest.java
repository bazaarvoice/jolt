package com.bazaarvoice.jolt;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class ShiftrTest {

    // TODO: test arrays better (wildcards test array could be in reverse order)
    @DataProvider
    public Object[][] getTestCaseUnits() {
        return new Object[][] {
            {"arrayMismatch"},
            {"bucketToPrefixSoup"},
            {"declaredOutputArray"},
            {"explicitArrayKey"},
            {"firstSample"},
            {"identity"},
            {"inputArrayToPrefix"},
            {"invertMap"},
            {"keyref"},
            {"lhsAmpMatch"},
            {"listKeys"},
            {"mapToList"},
            {"mapToList2"},
            {"multiPlacement"},
            {"objectToArray"},
            {"photosArray"},
            {"prefixDataToArray"},
            {"prefixedData"},
            {"prefixSoupToBuckets"},
            {"queryMappingXform"},
            {"singlePlacement"},
            {"specialKeys"},
            {"wildcards"},
            {"wildcardSelfAndRef"},
        };
    }

    // TODO: test arrays better (wildcards test array could be in reverse order)

    @Test(dataProvider = "getTestCaseUnits")
    public void runTestUnits(String testCaseName) throws IOException {

        String testPath = "/json/shiftr/" + testCaseName;
        Map<String, Object> testUnit = (Map<String, Object>) JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + ".json" ) );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Shiftr shiftr = new Shiftr( spec );
        Object actual = shiftr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
