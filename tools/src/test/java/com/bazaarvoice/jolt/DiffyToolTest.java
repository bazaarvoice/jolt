package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.io.IOException;

public class DiffyToolTest {

    @Test
    public void testRunDiffy()
            throws IOException {
        // The horrifying code you see below chooses the "correct" path to the test files to feed to the CLI.
        // When the test is run by maven the working directory is $JOLT_CHECKOUT/tools/. The code figures this out
        // by examining the last directory in the file path for the working directory. If it's 'tools' then it
        // chooses the path to the resource files copied by maven into the target/ directory. Obviously, this assumes
        // that you did not name $JOLT_CHECKOUT 'tools'. If that check fails then the path is chosen with the assumption
        // that the test is running in an IDE (Intellij IDEA in my case). Your mileage with other IDE's may very.
        String[] workingPath = System.getProperty( "user.dir" ).split( "/" );
        String path;
        if ( workingPath[workingPath.length - 1].equals( "tools" ) ) {
            // This test is being run by maven
            path = "target/test-classes/json/";
        }
        else {
            // This test is being run in an IDE (IntelliJ IDEA)
            path = "tools/src/test/resources/json/";
        }

        // Input with no differences should return true
        AssertJUnit.assertTrue( DiffyTool.runDiffy( new String[] {path + "input1.json", path + "input1.json"} ) );

        // Input with differences should return false
        AssertJUnit.assertFalse( DiffyTool.runDiffy( new String[] {path + "input1.json", path + "input2.json"} ) );
    }
}
