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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DiffyTest {

    private Diffy unit;

    @BeforeMethod
    public void setup() {
        this.unit = new Diffy();
    }

    @AfterMethod
    public void teardown() {
        this.unit = null;
    }

    private void testScalars(Object expected, Object actual, boolean expectDiff) {
        Diffy.Result result = this.unit.diff( expected, actual );
        if (expectDiff) {
            AssertJUnit.assertEquals( expected, result.expected );
            AssertJUnit.assertEquals( actual, result.actual );
        }
    }

    private static final Object[] SCALARS = new Object[] {
            null, 1, 2, true, false, 3.14, 2.71, "foo", "bar", new ArrayList(), new HashMap()
    };

    @Test
    public void testAllTheScalars() {
        for (int i=0; i<SCALARS.length; i++) {
            for (int j=i; j<SCALARS.length; j++) {
                this.testScalars( SCALARS[i], SCALARS[j], i!=j );
            }
        }
    }

    @Test
    public void diff_itSaysListsWithSameElementsAreSame() {
        Object[] stuff = new Object[] {"foo", 3, null};
        List list1 = Arrays.asList( stuff );
        List list2 = Arrays.asList( stuff );
        Diffy.Result result = this.unit.diff( list1, list2 );
        AssertJUnit.assertTrue( result.isEmpty() );
    }

    @Test
    public void diff_itRecognizesDifferingElementsInArrays() {
        Diffy.Result result = this.unit.diff(
                Arrays.asList( new Object[] {"foo", 3, null} ),
                Arrays.asList( new Object[] {"foo", 3, "apple"} ) );
        AssertJUnit.assertEquals( Arrays.asList( new Object[] {null, null, null} ), result.expected );
        AssertJUnit.assertEquals( Arrays.asList( new Object[] { null, null, "apple" } ), result.actual );
    }

    @Test
    public void diff_itHandlesLongerExpectedArray() {
        Diffy.Result result = this.unit.diff(
                Arrays.asList( new Object[] { "foo", 3, true } ),
                Arrays.asList( new Object[] { "foo", 3 } ) );
        AssertJUnit.assertEquals( Arrays.asList( new Object[] {null, null, true} ), result.expected );
        AssertJUnit.assertEquals( Arrays.asList( new Object[] {null, null} ), result.actual );
    }

    @Test
    public void diff_itHandlesLongerActualArray() {
        Diffy.Result result = this.unit.diff(
                Arrays.asList( new Object[] {"foo", 3} ),
                Arrays.asList( new Object[] {"foo", 3, false} ) );
        AssertJUnit.assertEquals( Arrays.asList( new Object[] {null, null} ), result.expected );
        AssertJUnit.assertEquals( Arrays.asList( new Object[] {null, null, false} ), result.actual );
    }

    @Test
    public void diff_itSaysMapsWithSameContentsAreSame()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ),
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ) );
        AssertJUnit.assertTrue( result.isEmpty() );
    }

    @Test
    public void diff_itSaysDifferentWhenActualHasExtra()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ),
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\", \"extra\":null}" ) );
        AssertJUnit.assertEquals( new HashMap(), result.expected );
        AssertJUnit.assertEquals( JsonUtils.jsonToMap( "{\"extra\":null}" ) , result.actual );
    }

    @Test
    public void diff_itSaysDifferentWhenExpectedHasExtra()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\", \"extra\":42}" ),
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ) );
        AssertJUnit.assertEquals( JsonUtils.jsonToMap( "{\"extra\":42}" ), result.expected );
        AssertJUnit.assertEquals( new HashMap(), result.actual );
    }

    @Test
    public void diff_itSaysDifferentWhenAnAttributeDiffers()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ),
                JsonUtils.jsonToMap( "{\"foo\":\"apple\", \"bar\":\"baz\"}" ) );
        AssertJUnit.assertEquals( JsonUtils.jsonToMap( "{\"foo\":1}" ), result.expected );
        AssertJUnit.assertEquals( JsonUtils.jsonToMap( "{\"foo\":\"apple\"}" ), result.actual );
    }

}
