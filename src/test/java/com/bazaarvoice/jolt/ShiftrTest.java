package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nate.forman
 * Date: 8/3/12
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
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
            // TODO enable these tests when Shiftr is updated to support array and key mashing features
            //{"photosArray", null, null, null},
            //{"bucketToPrefixSoup", null, null, null},
            //{"prefixSoupToBuckets", null, null, null},
            {"firstSample", null, null, null},
            {"singlePlacement", null, null, null},
            {"multiPlacement", null, null, null},
            {"wildcards", null, null, null},
            {"specialKeys", null, null, null},
            {"identity", null, null, null},
            {"objectToArray", null, null, null},
            {"keyref", null, null, null},
            {"queryMappingXform", null, null, null},
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

        Shiftr jolt = new Shiftr();
        Object actual = jolt.xform( input, spec );

        Diffy diffy = new ArrayDisorderDiffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "failed case "+testPath+".\nhere is a diff:\nexpected: "+JsonUtils.toJsonString( result.expected )+"\nactual: "+JsonUtils.toJsonString( result.actual ) );
        }
        AssertJUnit.assertTrue( testPath, result.isEmpty() );
    }

    static class ArrayDisorderDiffy extends Diffy {
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
