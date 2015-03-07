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

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class JoltCliTest {

    @Test
    public void testRunJolt()
            throws IOException {
        // The horrifying code you see below chooses the "correct" path to the test files to feed to the CLI.
        // When the test is run by maven the working directory is $JOLT_CHECKOUT/cli/. The code figures this out
        // by examining the last directory in the file path for the working directory. If it's 'cli' then it
        // chooses the path to the resource files copied by maven into the target/ directory. Obviously, this assumes
        // that you did not name $JOLT_CHECKOUT 'cli'. If that check fails then the path is chosen with the assumption
        // that the test is running in an IDE (Intellij IDEA in my case). Your mileage with other IDE's may very.
        String path = System.getProperty( "user.dir" );
        if ( path.endsWith( "cli" ) ) {
            // This test is being run by maven
            path += "//target//test-classes//json//";
        }
        else {
            // This test is being run in an IDE (IntelliJ IDEA)
          path += "//cli//src//test//resources//json//";
        }

        // diffy: Input with no differences should return true
        Assert.assertTrue( JoltCli.runJolt( new String[] {"diffy", path + "input1.json", path + "input1.json", "-s"} ) );

        // diffy: Input with differences should return false
        Assert.assertFalse( JoltCli.runJolt( new String[] {"diffy", path + "input1.json", path + "input2.json", "-s"} ) );

        // sort: well formed input should return true
        Assert.assertTrue( JoltCli.runJolt( new String[] {"sort", path + "input1.json"} ) );

        // transform: well formed input should return true
        Assert.assertTrue( JoltCli.runJolt( new String[] {"transform", path + "spec.json", path + "transformInput.json"} ) );
    }
}
