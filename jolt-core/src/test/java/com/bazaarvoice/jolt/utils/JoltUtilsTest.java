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
package com.bazaarvoice.jolt.utils;

import com.bazaarvoice.jolt.Diffy;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.traversr.SimpleTraversal;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.bazaarvoice.jolt.utils.JoltUtils.isEmptyJson;
import static com.bazaarvoice.jolt.utils.JoltUtils.navigate;
import static com.bazaarvoice.jolt.utils.JoltUtils.navigateSafe;

public class JoltUtilsTest {

    private Diffy diffy = new Diffy();

    private Map ab = ImmutableMap.builder().put( "a", "b" ).build();
    private Map cd = ImmutableMap.builder().put( "c", "d" ).build();
    private Map top = ImmutableMap.builder().put( "A", ab ).put( "B", cd ).build();

    private Object[] flattenedValues = {0, 1, 2, 1.618, "m", "n", 1, 2, 3.14159, "y" };
    private String jsonSourceString = "{ " +
            "    \"a\": { " +
            "        \"b\": [ " +
            "            0, " +
            "            1, " +
            "            2, " +
            "            1.618 " +
            "        ] " +
            "    }, " +
            "    \"p\": [ " +
            "        \"m\", " +
            "        \"n\", " +
            "        { " +
            "            \"1\": 1, " +
            "            \"2\": 2, " +
            "            \"pi\": 3.14159 " +
            "        } " +
            "    ], " +
            "    \"x\": \"y\" " +
            "}\n";

    private String jsonSourceString_empty =
            "{" +
                    "\"e\": {" +
                    "\"f\": {}," +
                    "\"g\": []" +
                    "}," +
                    "\"h\": [" +
                    "{}," +
                    "[]" +
                    "]" +
                    "}";

    private Object jsonSource;
    private Object jsonSource_empty;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public void setup() throws IOException {
        jsonSource = JsonUtils.jsonToObject(jsonSourceString);
        jsonSource_empty = JsonUtils.jsonToObject(jsonSourceString_empty);
    }

    @DataProvider
    public Object[][] removeRecursiveCases() {

        Map empty = ImmutableMap.builder().build();
        Map barToFoo = ImmutableMap.builder().put( "bar", "foo" ).build();
        Map fooToBar = ImmutableMap.builder().put( "foo", "bar" ).build();
        return new Object[][] {
                { null, null, null },
                { null, "foo", null },
                { "foo", null, "foo" },
                { "foo", "foo", "foo" },
                { Maps.newHashMap(), "foo", empty },
                { Maps.newHashMap( barToFoo ), "foo", barToFoo },
                { Maps.newHashMap( fooToBar ), "foo", empty },
                { Lists.newArrayList(), "foo", ImmutableList.builder().build() },
                {
                        Lists.newArrayList( ImmutableList.builder()
                                .add( Maps.newHashMap( barToFoo ) )
                                .build() ),
                        "foo",
                        ImmutableList.builder()
                                .add( barToFoo )
                                .build()
                },
                {
                        Lists.newArrayList( ImmutableList.builder()
                                .add( Maps.newHashMap( fooToBar ) )
                                .build() ),
                        "foo",
                        ImmutableList.builder()
                                .add( empty )
                                .build()
                }
        };
    }

    @Test(dataProvider = "removeRecursiveCases")
    public void testRemoveRecursive(Object json, String key, Object expected) throws IOException {

        JoltUtils.removeRecursive( json, key );

        Diffy.Result result = diffy.diff( expected, json );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }

    @Test
    public void runFixtureTests() throws IOException {

        String testFixture = "/json/utils/joltUtils-removeRecursive.json";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tests = (List<Map<String, Object>>) JsonUtils.classpathToObject( testFixture );

        for ( Map<String,Object> testUnit : tests ) {

            Object data = testUnit.get( "input" );
            String toRemove = (String) testUnit.get( "remove" );
            Object expected = testUnit.get( "expected" );

            JoltUtils.removeRecursive( data, toRemove );

            Diffy.Result result = diffy.diff( expected, data );
            if (!result.isEmpty()) {
                Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
            }
        }
    }

    @Test( description = "No exception if we don't try to remove from an ImmutableMap.")
    public void doNotUnnecessarilyDieOnImmutableMaps() throws IOException
    {
        Map expected = JsonUtils.jsonToMap( JsonUtils.toJsonString( top ) );

        JoltUtils.removeRecursive( top, "tuna" );

        Diffy.Result result = diffy.diff( expected, top );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }

    @Test( expectedExceptions = UnsupportedOperationException.class, description = "Exception if try to remove from an Immutable map.")
    public void correctExceptionWithImmutableMap() throws IOException
    {
        JoltUtils.removeRecursive( top, "c" );
    }

    @DataProvider (parallel = true)
    public Iterator<Object[]> coordinates() throws IOException {
        List<Object[]> testCases = new ArrayList<>();

        testCases.add(new Object[] { 0, new Object[] {"a", "b", 0}} );
        testCases.add(new Object[] { 1, new Object[] {"a", "b", 1}} );
        testCases.add(new Object[] { 2, new Object[] {"a", "b", 2}} );
        testCases.add(new Object[] { 1.618, new Object[] {"a", "b", 3}} );
        testCases.add(new Object[] { "m", new Object[] {"p", 0}} );
        testCases.add(new Object[] { "n", new Object[] {"p", 1}} );
        testCases.add(new Object[] { 1, new Object[] {"p", 2, "1"}} );
        testCases.add(new Object[] { 2, new Object[] {"p", 2, "2"}} );
        testCases.add(new Object[] { 3.14159, new Object[] {"p", 2, "pi"}} );
        testCases.add(new Object[] { "y", new Object[] {"x"}} );

        testCases.add(new Object[] { ((Map) jsonSource).get("a"), new Object[] {"a"}} );
        testCases.add(new Object[] { ((Map)(((Map) jsonSource).get("a"))).get("b"), new Object[] {"a", "b"}} );
        testCases.add(new Object[] { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(0), new Object[] {"a", "b", 0}} );
        testCases.add(new Object[] { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(1), new Object[] {"a", "b", 1}} );
        testCases.add(new Object[] { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(2), new Object[] {"a", "b", 2}} );
        testCases.add(new Object[] { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(3), new Object[] {"a", "b", 3}} );
        testCases.add(new Object[] { ((Map) jsonSource).get("p"), new Object[] {"p"}} );
        testCases.add(new Object[] { ((List)(((Map) jsonSource).get("p"))).get(0), new Object[] {"p", 0}} );
        testCases.add(new Object[] { ((List)(((Map) jsonSource).get("p"))).get(1), new Object[] {"p", 1}} );
        testCases.add(new Object[] { ((List)(((Map) jsonSource).get("p"))).get(2), new Object[] {"p", 2}} );
        testCases.add(new Object[] { ((Map)((List)(((Map) jsonSource).get("p"))).get(2)).get("1"), new Object[] {"p", 2, "1"}} );
        testCases.add(new Object[] { ((Map)((List)(((Map) jsonSource).get("p"))).get(2)).get("2"), new Object[] {"p", 2, "2"}} );
        testCases.add(new Object[] { ((Map)((List)(((Map) jsonSource).get("p"))).get(2)).get("pi"), new Object[] {"p", 2, "pi"}} );
        testCases.add(new Object[] { ((Map) jsonSource).get("x"), new Object[] {"x"}} );

        return testCases.iterator();
    }

    /**
     * Method: navigate(Object source, Object... paths)
     */
    @Test (dataProvider = "coordinates")
    public void navigator(Object expected, Object[] path) throws Exception {
        Object actual = navigate(jsonSource, path);
        Assert.assertEquals(actual, expected);
    }

    @DataProvider (parallel = true)
    public Iterator<Object[]> coordinates_simple() throws IOException {
        List<Object[]> testCases = new ArrayList<>();

        testCases.add(new Object[] { 0, new Object[] {"a", "b", 0}} );
        testCases.add(new Object[] { 1, new Object[] {"a", "b", 1}} );
        testCases.add(new Object[] { 2, new Object[] {"a", "b", 2}} );
        testCases.add(new Object[] { 1.618, new Object[] {"a", "b", 3}} );
        testCases.add(new Object[] { "m", new Object[] {"p", 0}} );
        testCases.add(new Object[] { "n", new Object[] {"p", 1}} );
        testCases.add(new Object[] { 1, new Object[] {"p", 2, "1"}} );
        testCases.add(new Object[] { 2, new Object[] {"p", 2, "2"}} );
        testCases.add(new Object[] { 3.14159, new Object[] {"p", 2, "pi"}} );
        testCases.add(new Object[] { "y", new Object[] {"x"}} );

        return testCases.iterator();
    }

    @Test (dataProvider = "coordinates_simple")
    public void navigatorSafe(Object expected, Object[] path) throws Exception {
        Object actual = navigateSafe(expected, jsonSource_empty, path);
        Assert.assertEquals(actual, expected);
    }


    @Test
    public void testIsEmptyJson() {
        Assert.assertFalse(isEmptyJson(jsonSource));
        Assert.assertTrue(isEmptyJson(jsonSource_empty));
    }

    @Test
    public void testListKeyChains() {
        jsonSource = JsonUtils.jsonToObject(jsonSourceString);
        List<Object[]> keyChains = JoltUtils.listKeyChains(jsonSource);
        for(int i=0; i<keyChains.size(); i++) {
            Object[] keyChain = keyChains.get(i);
            Object expected = flattenedValues[i];
            Object actual = navigate(jsonSource, keyChain);
            Assert.assertEquals(actual, expected);
        }

        keyChains = JoltUtils.listKeyChains(jsonSource_empty);
        for(Object[] keyChain: keyChains) {
            Assert.assertTrue(isEmptyJson(navigate(jsonSource_empty, keyChain)));
        }
    }

    @DataProvider(parallel = false)
    public Iterator<Object[]> pathProvider() {
        return JoltUtils.listKeyChains(jsonSource).iterator();
    }

    @Test(dataProvider = "pathProvider")
    public void testToSimpleTraversrPath(Object... paths) {
        String humanReadablePath = JoltUtils.toSimpleTraversrPath(paths);
        Assert.assertEquals(new SimpleTraversal<>(humanReadablePath).get(jsonSource).get(), navigate(jsonSource, paths));
    }

    @Test
    public void testE2E() {
        Object duplicate = Maps.newHashMap();
        for(Object[] paths : JoltUtils.listKeyChains(jsonSource)) {
            String humanReadablePath = JoltUtils.toSimpleTraversrPath(paths);
            new SimpleTraversal<>(humanReadablePath).set(duplicate, navigate(jsonSource, paths));
        }
        Assert.assertTrue(diffy.diff(jsonSource, duplicate).isEmpty());
    }
}
