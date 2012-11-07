package com.bazaarvoice.jolt;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShiftrTest {

    @Test(expectedExceptions = JoltException.class)
    public void process_itBlowsUpForNoSpec()
            throws JoltException {
        Map<String, Object> opEntry = new HashMap<String, Object>();
        Object input = new Object();
        Shiftr unit = new Shiftr();
        unit.process( input, opEntry );
    }

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
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

        Shiftr shiftr = new Shiftr();
        Object actual = shiftr.xform( input, spec );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
