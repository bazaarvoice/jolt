package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;

import java.io.IOException;

public class JoltTestUtil {

    private static Diffy diffy = new Diffy();

    public static void runDiffy( String failureMessage, Object expected, Object actual ) throws IOException {

        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( failureMessage + ".\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }

    public static void runDiffy( Object expected, Object actual ) throws IOException {
        runDiffy( "Failed", expected, actual );
    }
}
