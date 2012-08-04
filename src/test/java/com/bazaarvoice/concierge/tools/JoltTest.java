package com.bazaarvoice.concierge.tools;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nate.forman
 * Date: 8/3/12
 * Time: 11:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class JoltTest {

    private static String[] TEST_CASES = new String[] {
      "firstSample"
    };

    @Test
    public void runTestCases()
            throws IOException {
        for (int i=0; i<TEST_CASES.length; i++) {
            String testPath = "/json/jolt/"+TEST_CASES[i];
            Map<String, Object> input = JsonUtils.jsonToMap( Jolt.class.getResourceAsStream( testPath+"/input.json" ) );
            Map<String, Object> spec = JsonUtils.jsonToMap( Jolt.class.getResourceAsStream( testPath+"/spec.json" ) );
            Map<String, Object> expected = JsonUtils.jsonToMap( Jolt.class.getResourceAsStream( testPath+"/output.json" ) );

            Jolt jolt = new Jolt();
            Map<String, Object> actual = jolt.xform( input, spec );

            Diffy diffy = new Diffy();
            Diffy.Result result = diffy.diff( expected, actual );
            if (!result.isEmpty()) {
                AssertJUnit.fail( "failed case "+testPath+", diff:\nexpected: "+JsonUtils.toJsonString( result.expected )+"\nactual: "+JsonUtils.toJsonString( result.actual ) );
            }
            AssertJUnit.assertTrue( testPath, result.isEmpty() );
        }
    }
}
