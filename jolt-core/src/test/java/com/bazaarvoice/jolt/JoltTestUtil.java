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
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

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

        // Special case: Object[]. Lists.newList(Object[]) simply produces a
        // list of one element, which is the Object[]. It's just a wrapper.
        // We need to actually transplant the elements the Object[] to a List.
        actual = (actual instanceof Object[]) ? copyElementsToList((Object[]) actual) : actual;
        expected = (expected instanceof Object[]) ? copyElementsToList((Object[]) expected) : expected;

        // If the values are scalar, need to change to List of one element
        // so Diffy can compare them. In this way, other Jolt methods can still
        // test scalar values, and Diffy.diff will still function.
        actual = JsonUtils.isJSONType(actual) ? actual : Lists.newArrayList(actual);
        expected = JsonUtils.isJSONType(expected) ? expected : Lists.newArrayList(expected);

        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            Assert.fail( "\nActual object\n" + actualObject + "\n" + failureMessage + "\nDiffy output\n" + result.toString());
        }
    }

    /**
     * Copies elements one-by-one into a new List. Solves the problem of
     * Lists being just wrappers around an array, and not actually making the
     * O(n) operation to copy each element.
     *
     * @param array the array whose elements will be moved to a List
     * @return a List containing the array's elements, in the same order
     */
    private static List<Object> copyElementsToList(Object[] array) {
        List<Object> newList = new LinkedList<>();
        for (int i = 0; i < array.length; i++) {
            newList.add(array[i]);
        }
        return newList;
    }
}
