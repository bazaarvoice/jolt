package com.bazaarvoice.concierge.tools;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: nate.forman
 * Date: 8/3/12
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoltTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
            {"firstSample"},
            {"singlePlacement"},
            {"multiPlacement"},
            {"wildcards"},
            {"specialKeys"},
            {"identity"},
            {"objectToArray"},
            {"keyref"},
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
        String testPath = "/json/jolt/"+testCaseName;
        Object input = JsonUtils.jsonToObject( Jolt.class.getResourceAsStream( testPath + "/input.json" ) );
        Object spec = JsonUtils.jsonToObject( Jolt.class.getResourceAsStream( testPath + "/spec.json" ) );
        Object expected = JsonUtils.jsonToObject( Jolt.class.getResourceAsStream( testPath + "/output.json" ) );

        Jolt jolt = new Jolt();
        Object actual = jolt.xform( input, spec );

        Diffy diffy = new ArrayDisorderDiffy();
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "failed case "+testPath+".\nhere is a diff:\nexpected: "+JsonUtils.toJsonString( result.expected )+"\nactual: "+JsonUtils.toJsonString( result.actual ) );
        }
        AssertJUnit.assertTrue( testPath, result.isEmpty() );
    }

    private class ArrayDisorderDiffy extends Diffy {
        Result diffList(List expected, List actual) {
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
