package com.bazaarvoice.concierge.tools;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: nate.forman
 * Date: 8/3/12
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoltTest {

    private static String[] TEST_CASES = new String[] {
      "firstSample",
      "singlePlacement",
      "multiPlacement",
      "wildcards",
      "specialKeys",
      "identity",
      "objectToArray",
      ""
    };

    // TODO: test arrays better (wildcards test array could be in reverse order)

    @Test
    public void runTestCases()
            throws IOException {
        for (int i=0; i<TEST_CASES.length; i++) {
            if ("".equals( TEST_CASES[i] )) {
                continue;
            }
            System.out.println("Running test case "+TEST_CASES[i]);
            String testPath = "/json/jolt/"+TEST_CASES[i];
            Object input = JsonUtils.jsonToObject( Jolt.class.getResourceAsStream( testPath+"/input.json" ) );
            Object spec = JsonUtils.jsonToObject( Jolt.class.getResourceAsStream( testPath+"/spec.json" ) );
            Object expected = JsonUtils.jsonToObject( Jolt.class.getResourceAsStream( testPath+"/output.json" ) );

            Jolt jolt = new Jolt();
            Object actual = jolt.xform( input, spec );

            Diffy diffy = new Diffy();
            Diffy.Result result = diffy.diff( expected, actual );
            if (!result.isEmpty()) {
                AssertJUnit.fail( "failed case "+testPath+".\nhere is a diff:\nexpected: "+JsonUtils.toJsonString( result.expected )+"\nactual: "+JsonUtils.toJsonString( result.actual ) );
            }
            AssertJUnit.assertTrue( testPath, result.isEmpty() );
        }
    }
}
