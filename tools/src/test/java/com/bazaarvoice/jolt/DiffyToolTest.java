/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        String path = System.getProperty( "user.dir" );
        if ( path.endsWith( "tools" ) ) {
            // This test is being run by maven
            path += "//target//test-classes//json//";
        }
        else {
            // This test is being run in an IDE (IntelliJ IDEA)
          path += "//tools//src//test//resources//json//";
        }

        // Input with no differences should return true
        AssertJUnit.assertTrue( DiffyTool.runDiffy( new String[] {path + "input1.json", path + "input1.json"} ) );

        // Input with differences should return false
        AssertJUnit.assertFalse( DiffyTool.runDiffy( new String[] {path + "input1.json", path + "input2.json"} ) );
    }
}
