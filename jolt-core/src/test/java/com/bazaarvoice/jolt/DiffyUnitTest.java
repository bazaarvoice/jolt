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
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiffyUnitTest {

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
            Assert.assertEquals( expected, result.expected );
            Assert.assertEquals( actual, result.actual );
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
        Assert.assertTrue( result.isEmpty() );
    }

    @Test
    public void diff_itRecognizesDifferingElementsInArrays() {
        Diffy.Result result = this.unit.diff(
                Arrays.asList( "foo", 3, null ),
                Arrays.asList( "foo", 3, "apple" ) );
        Assert.assertEquals( Arrays.asList( new Object[] {null, null, null} ), result.expected );
        Assert.assertEquals( Arrays.asList( new Object[] { null, null, "apple" } ), result.actual );
    }

    @Test
    public void diff_itHandlesLongerExpectedArray() {
        Diffy.Result result = this.unit.diff(
                Arrays.asList( "foo", 3, true ),
                Arrays.asList( "foo", 3 ) );
        Assert.assertEquals( Arrays.asList( new Object[] {null, null, true} ), result.expected );
        Assert.assertEquals( Arrays.asList( new Object[] {null, null} ), result.actual );
    }

    @Test
    public void diff_itHandlesLongerActualArray() {
        Diffy.Result result = this.unit.diff(
                Arrays.asList( "foo", 3 ),
                Arrays.asList( "foo", 3, false ) );
        Assert.assertEquals( Arrays.asList( new Object[] {null, null} ), result.expected );
        Assert.assertEquals( Arrays.asList( new Object[] {null, null, false} ), result.actual );
    }

    @Test
    public void diff_itSaysMapsWithSameContentsAreSame()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ),
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ) );
        Assert.assertTrue( result.isEmpty() );
    }

    @Test
    public void diff_itSaysDifferentWhenActualHasExtra()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ),
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\", \"extra\":null}" ) );
        Assert.assertEquals( new HashMap(), result.expected );
        Assert.assertEquals( JsonUtils.jsonToMap( "{\"extra\":null}" ) , result.actual );
    }

    @Test
    public void diff_itSaysDifferentWhenExpectedHasExtra()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\", \"extra\":42}" ),
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ) );
        Assert.assertEquals( JsonUtils.jsonToMap( "{\"extra\":42}" ), result.expected );
        Assert.assertEquals( new HashMap(), result.actual );
    }

    @Test
    public void diff_itSaysDifferentWhenAnAttributeDiffers()
            throws IOException {
        Diffy.Result result = this.unit.diff(
                JsonUtils.jsonToMap( "{\"foo\":1, \"bar\":\"baz\"}" ),
                JsonUtils.jsonToMap( "{\"foo\":\"apple\", \"bar\":\"baz\"}" ) );
        Assert.assertEquals( JsonUtils.jsonToMap( "{\"foo\":1}" ), result.expected );
        Assert.assertEquals( JsonUtils.jsonToMap( "{\"foo\":\"apple\"}" ), result.actual );
    }

    /**
     * Testing / exploring basic Map.equals behavior.
     */
    @Test
    public void verify_NestedMapEquals() {
        Map<String,Object> h1 = new HashMap<>();
        {
            h1.put( "a", "a" );
            Map<String,Object> bMap = new HashMap<>();
            bMap.put( "c", "c" );
            bMap.put( "d", "d" );
            h1.put( "b", bMap );
        }

        Map<String,Object> h2 = new HashMap<>();
        {
            Map<String,Object> bMap = new HashMap<>();
            bMap.put( "c", "c" );
            bMap.put( "d", "d" );
            h2.put( "b", bMap );
            h2.put( "a", "a" );
        }

        Assert.assertTrue( h1.equals( h2 ), "1->2 Two HashMaps with the same things should be equal." );
        Assert.assertEquals( h1.hashCode(), h2.hashCode(), "1->2 Two HashMaps with the same things should have the same hashCode." );

        Assert.assertTrue( h2.equals( h1 ), "2->1 Two HashMaps with the same things should be equal." );
        Assert.assertEquals( h2.hashCode(), h1.hashCode(), "2->1 Two HashMaps with the same things should have the same hashCode." );

        Map<String,Object> lh1 = new LinkedHashMap<>();
        {
            lh1.put( "a", "a" );
            Map<String,Object> bMap = new HashMap<>();
            bMap.put( "c", "c" );
            bMap.put( "d", "d" );
            lh1.put( "b", bMap );
        }

        Assert.assertTrue( lh1.equals( h2 ), "lh1->2 Two HashMaps with the same things should be equal." );
        Assert.assertEquals( lh1.hashCode(), h2.hashCode(), "lh1->2 Two HashMaps with the same things should have the same hashCode." );

        Assert.assertTrue( lh1.equals( h1 ), "lh1->1 Two HashMaps with the same things should be equal." );
        Assert.assertEquals( lh1.hashCode(), h1.hashCode(), "lh1->1 Two HashMaps with the same things should have the same hashCode." );

        Map<String,Object> d1 = new HashMap<>();
        {
            d1.put( "a", "a" );
            Map<String,Object> bMap = new HashMap<>();
            bMap.put( "c", "c" );
            bMap.put( "d", "E" );
            d1.put( "b", bMap );
        }

        Assert.assertFalse( d1.equals( h2 ), "Two HashMaps that should not be equal." );
        Assert.assertNotEquals( d1.hashCode(), h2.hashCode(), "lh1->2 Two HashMaps should not have the same hashCode ." );
    }
}
