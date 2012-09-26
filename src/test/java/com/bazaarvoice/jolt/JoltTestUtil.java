package com.bazaarvoice.jolt;

import org.testng.AssertJUnit;

import java.io.IOException;
import java.util.List;

public class JoltTestUtil {

    private static ArrayDisorderDiffy diffy = new ArrayDisorderDiffy();

    public static void runDiffy( Object expected, Object actual, String failureMessage ) throws IOException {

        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            AssertJUnit.fail( failureMessage + ".\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
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
