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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.bazaarvoice.jolt.utils.JoltUtils.isBlankJson;
import static com.bazaarvoice.jolt.utils.JoltUtils.isVacantJson;
import static com.bazaarvoice.jolt.utils.JoltUtils.navigate;

public class JoltUtilsTest {

    private Diffy diffy = new Diffy();

    private Object[] flattenedValues = {0, 1, 2, 1.618, "m", "n", 1, 2, 3.14159, "y" };

    private Object jsonSource;
    private Object jsonSource_empty;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public void setup() {
        String jsonSourceString = "{ " +
            "    'a': { " +
            "        'b': [ 0, 1, 2, 1.618 ] " +
            "    }, " +
            "    'p': [ 'm', 'n', " +
            "        { " +
            "            '1': 1, " +
            "            '2': 2, " +
            "            'pi': 3.14159 " +
            "        } " +
            "    ], " +
            "    'x': 'y' " +
            "}\n";

        jsonSource = JsonUtils.javason(jsonSourceString);

        String jsonSourceString_empty =
            "{" +
                "'e': { 'f': {}, 'g': [] }," +
                "'h': [ {}, [] ]" +
            "}";

        jsonSource_empty = JsonUtils.javason(jsonSourceString_empty);
    }

    @Test
    public void testIsEmptyJson() {
        Assert.assertFalse(isVacantJson(jsonSource));
        Assert.assertFalse(isBlankJson(jsonSource));

        Assert.assertTrue(isVacantJson(jsonSource_empty));
        Assert.assertFalse(isBlankJson(jsonSource_empty));

        Assert.assertTrue(isBlankJson(ImmutableMap.of()));
        Assert.assertTrue(isBlankJson(ImmutableList.of()));
        Assert.assertTrue(isVacantJson(ImmutableMap.of()));
        Assert.assertTrue(isVacantJson(ImmutableList.of()));
    }

    @Test
    public void testListKeyChains() {
        List<Object[]> keyChains = JoltUtils.listKeyChains(jsonSource);
        for(int i=0; i<keyChains.size(); i++) {
            Object[] keyChain = keyChains.get(i);
            Object expected = flattenedValues[i];
            Object actual = navigate(jsonSource, keyChain);
            Assert.assertEquals(actual, expected);
        }

        keyChains = JoltUtils.listKeyChains(jsonSource_empty);
        for(Object[] keyChain: keyChains) {
            Assert.assertTrue(isVacantJson(navigate(jsonSource_empty, keyChain)));
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

    @DataProvider
    public Iterator<Object[]> storeTestCases() {

        String testFixture = "/json/utils/joltUtils-store-remove-compact.json";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tests = (List<Map<String, Object>>) JsonUtils.classpathToObject( testFixture );

        List<Object[]> testCases = new LinkedList<>();

        for(Map<String, Object> testCase: tests) {
            testCases.add(new Object[] {
                    testCase.get("description"),
                    testCase.get("source"),
                    ((List)testCase.get("path")).toArray(),
                    testCase.get("value"),
                    testCase.get("output")
            });
        }

        return testCases.iterator();
    }

    /**
     * Given a source, an output, and a pair of path-to-values, stores-then-validates-then-removes those
     * resulting in a mutated source, which is finally compacted and matched with given output
     */
    @Test (dataProvider = "storeTestCases")
    public void testStoreRemoveCompact(String description, Object source, Object[] path, Object value, Object output) {

        Object existingValue = JoltUtils.navigateOrDefault(null, source, path);
        Assert.assertEquals( JoltUtils.store(source, value, path), existingValue);
        Assert.assertEquals( JoltUtils.remove( source, path ), value );

        // check the json object
        int noCompactionSize = JoltUtils.listKeyChains(source).size();
        JoltUtils.compactJson(source); // source is compacted now
        int compactedSize = JoltUtils.listKeyChains(source).size();

        Assert.assertTrue(noCompactionSize >= compactedSize);
        Assert.assertTrue(diffy.diff(output, source).isEmpty());
    }
}