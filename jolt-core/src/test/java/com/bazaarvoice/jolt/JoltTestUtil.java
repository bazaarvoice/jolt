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

import java.io.IOException;

public class JoltTestUtil {

    private static final Diffy diffy = new Diffy();
    private static final Diffy arrayOrderObliviousDiffy = new ArrayOrderObliviousDiffy();

    public static void runDiffy( String failureMessage, Object expected, Object actual ) throws IOException {
        runDiffy( diffy, failureMessage, expected, actual );
    }

    public static void runDiffy( Object expected, Object actual ) throws IOException {
        runDiffy( diffy, "Failed", expected, actual );
    }

    public static void runArrayOrderObliviousDiffy( String failureMessage, Object expected, Object actual ) throws IOException {
        runDiffy( arrayOrderObliviousDiffy, failureMessage, expected, actual );
    }

    public static void runArrayOrderObliviousDiffy( Object expected, Object actual ) throws IOException {
        runDiffy( arrayOrderObliviousDiffy, "Failed", expected, actual );
    }


    private static void runDiffy( Diffy diffy, String failureMessage, Object expected, Object actual ) {
        String actualObject = JsonUtils.toPrettyJsonString( actual );
        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            Assert.fail( "\nActual object\n" + actualObject + "\n" + failureMessage + "\nDiffy output\n" + result.toString());
        }
    }
}
