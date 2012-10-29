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
            {"photosArray", null, null, null},
            {"bucketToPrefixSoup", null, null, null},
            {"prefixSoupToBuckets", null, null, null},
            {"firstSample", null, null, null},
            {"singlePlacement", null, null, null},
            {"multiPlacement", null, null, null},
            {"wildcards", null, null, null},
            {"specialKeys", null, null, null},
            {"identity", null, null, null},
            {"objectToArray", null, null, null},
            {"keyref", null, null, null},
            {"queryMappingXform", null, null, null},
            {"wildcardSelfAndRef", null, null, null},
            {"", null, null, null}
        };
    }

    // TODO: test arrays better (wildcards test array could be in reverse order)

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName, String inputPath, String specPath, String outputPath)
            throws IOException {
        if ("".equals( testCaseName )) {
            return;
        }
        String testPath = "/json/shiftr/"+testCaseName;
        Object input = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( inputPath == null ? testPath + "/input.json" : inputPath ) );
        Object spec = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( specPath == null ? testPath + "/spec.json" : specPath ) );
        Object expected = JsonUtils.jsonToObject( Shiftr.class.getResourceAsStream( outputPath == null ? testPath + "/output.json" : outputPath ) );

        Shiftr shiftr = new Shiftr();
        Object actual = shiftr.xform( input, spec );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
