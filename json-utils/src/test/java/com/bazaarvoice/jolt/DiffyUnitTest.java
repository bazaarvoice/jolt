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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import static com.bazaarvoice.jolt.JsonUtils.isJSONType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
        Diffy.Result result = null;
        try {
            result = this.unit.diff(expected, actual);
        } catch (IllegalArgumentException e) {
            if (isJSONType(actual) && isJSONType(expected)) {
                Assert.fail("Map/List parameters are legal diff arguments, " +
                "but threw IllegalArgumentException: " + e.getMessage());
            }
        }

        if (null != result && expectDiff) {
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
     * Generates all combinations of argument types from a set of basic
     * arguments: primitives, Object, Maps, and Lists. Associated with each
     * type is a boolean, indicating whether the argument type is valid or not.
     * For our purposes, only Maps and Lists are valid.
     *
     * Each combination contains the 'expected' and 'actual' arguments for
     * Diffy.diff, as well as a boolean indicating whether the combination is
     * valid or not.
     *
     * @return a list of all combinations of argument types, alongside a boolean
     *         indicating whether the combination is valid or not
     */
    @DataProvider
    public static Iterator<Object[]> provideAllCombinationsOfArgumentTypes() {
        List<Object[]> argumentTypes = Lists.newArrayList();

        // Test all primitives
        argumentTypes.add(new Object[] {null,  false});
        argumentTypes.add(new Object[] {true,  false});
        argumentTypes.add(new Object[] {(byte) 0,  false});
        argumentTypes.add(new Object[] {(short) 0,  false});
        argumentTypes.add(new Object[] {0L,  false});
        argumentTypes.add(new Object[] {0.0f,  false});
        argumentTypes.add(new Object[] {0.0d,  false});
        argumentTypes.add(new Object[] {'a',  false});
        argumentTypes.add(new Object[] {"",  false});

        // Make sure it's not just that all Objects are accepted
        argumentTypes.add(new Object[] {new Object(),  false});

        // Looks like JSON, but not JSON
        argumentTypes.add(new Object[] {"\"key\": \"value\"",  false});
        argumentTypes.add(new Object[] {"{\n\"key\": \"value\"\n}",  false});
        argumentTypes.add(new Object[] {"{\n\"key\": [\"value1\", \"value2\"]\n}",  false});
        argumentTypes.add(new Object[] {"{\n\"key\": \n{\"subKey : \"value\"\n}\n}",  false});

        // Maps and Lists: only valid types
        argumentTypes.add(new Object[] {Lists.newArrayList(),  true});
        argumentTypes.add(new Object[] {new LinkedList(),  true});
        argumentTypes.add(new Object[] {new Stack(),  true});
        argumentTypes.add(new Object[] {Lists.newArrayList("value1", "value2", "value3"),  true});

        argumentTypes.add(new Object[] {Maps.newHashMap(),  true});
        argumentTypes.add(new Object[] {Maps.newLinkedHashMap(),  true});
        argumentTypes.add(new Object[] {Maps.newTreeMap(),  true});
        argumentTypes.add(new Object[] {ImmutableMap.builder().put("key1", "value1").put("key2", "value2").build(),  true});

        List<Object[]> allCombinations = new LinkedList<>();
        for (Object[] testCaseI : argumentTypes) {
            for (Object[] testCaseJ : argumentTypes) {
                Object expected = testCaseI[0];
                Object actual = testCaseJ[0];
                // Only a combination of two valid arguments is valid.
                boolean shouldNotThrowException = (boolean) testCaseI[1] && (boolean) testCaseJ[1];

                allCombinations.add(new Object[] {expected, actual, shouldNotThrowException});
            }
        }

        return allCombinations.iterator();
    }

    @Test (dataProvider = "provideAllCombinationsOfArgumentTypes")
    public void testInvalidArguments(Object expected, Object actual, boolean isValid) {
        boolean threwException = false;
        try {
            this.unit.diff(expected, actual);
        } catch (IllegalArgumentException e) {
            threwException = true;
        }

        // If we shouldn't have thrown an exception, but did, fail
        if (isValid && threwException) {
            Assert.fail("Threw IllegalArgumentException for valid arguments "
                    + expected + " and " + actual + " when should not have");
        }

        // If we should have thrown an exception, but did not, fail
        if (!isValid && !threwException) {
            Assert.fail("Did not throw IllegalArgumentException for invalid " +
                    "arguments " + expected + " and " + actual + " when should have");
        }
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
