package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShiftrTest {

    @Test(expectedExceptions = SpecException.class)
    public void process_itBlowsUpForNoSpec() {
        new Shiftr( null );
    }

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"declaredOutputArray"},
            {"photosArray"},
            {"inputArrayToPrefix"},
            {"explicitArrayKey"},
            {"prefixDataToArray"},
            {"bucketToPrefixSoup"},
            {"prefixSoupToBuckets"},
            {"firstSample"},
            {"singlePlacement"},
            {"multiPlacement"},
            {"wildcards"},
            {"specialKeys"},
            {"identity"},
            {"objectToArray"},
            {"keyref"},
            {"listKeys"},
            {"lhsAmpMatch"},
            {"queryMappingXform"},
            {"wildcardSelfAndRef"},
            {""}
        };
    }

    // TODO: test arrays better (wildcards test array could be in reverse order)

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName)
            throws IOException {
        if ("".equals( testCaseName )) {
            return;
        }
        String testPath = "/json/shiftr/"+testCaseName;
        Object input = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/input.json" ) );
        Object spec = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/spec.json" ) );
        Object expected = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( testPath + "/output.json" ) );

        Shiftr shiftr = new Shiftr( spec );
        Object actual = shiftr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
