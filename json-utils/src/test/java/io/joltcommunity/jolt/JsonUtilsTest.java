/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt;

import com.beust.jcommander.internal.Sets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import tools.jackson.core.type.TypeReference;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.*;
import java.util.*;

public class JsonUtilsTest {

    private Diffy diffy = new Diffy();

    private Map<Object, Object> ab = ImmutableMap.builder().put("a", "b").build();
    private Map<Object, Object> cd = ImmutableMap.builder().put("c", "d").build();
    private Map<Object, Object> top = ImmutableMap.builder().put("A", ab).put("B", cd).build();

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

    private Object jsonSource;

    @BeforeClass
    @SuppressWarnings("unchecked")
    public void setup() throws IOException {
        jsonSource = JsonUtils.jsonToObject(jsonSourceString);
        // added for type cast checking
        Set<String> aSet = Sets.newHashSet();
        aSet.add("i");
        aSet.add("j");
        ((Map) jsonSource).put("s", aSet);
    }

    @DataProvider
    public Object[][] removeRecursiveCases() {

        Map<Object, Object> empty = ImmutableMap.builder().build();
        Map<Object, Object> barToFoo = ImmutableMap.builder().put("bar", "foo").build();
        Map<Object, Object> fooToBar = ImmutableMap.builder().put("foo", "bar").build();
        return new Object[][]{
                {null, null, null},
                {null, "foo", null},
                {"foo", null, "foo"},
                {"foo", "foo", "foo"},
                {Maps.newHashMap(), "foo", empty},
                {Maps.newHashMap(barToFoo), "foo", barToFoo},
                {Maps.newHashMap(fooToBar), "foo", empty},
                {Lists.newArrayList(), "foo", ImmutableList.builder().build()},
                {
                        Lists.newArrayList(ImmutableList.builder()
                                .add(Maps.newHashMap(barToFoo))
                                .build()),
                        "foo",
                        ImmutableList.builder()
                                .add(barToFoo)
                                .build()
                },
                {
                        Lists.newArrayList(ImmutableList.builder()
                                .add(Maps.newHashMap(fooToBar))
                                .build()),
                        "foo",
                        ImmutableList.builder()
                                .add(empty)
                                .build()
                }
        };
    }


    @Test(dataProvider = "removeRecursiveCases")
    @SuppressWarnings("deprecation")
    public void testRemoveRecursive(Object json, String key, Object expected) throws IOException {

        JsonUtils.removeRecursive(json, key);

        Diffy.Result result = diffy.diff(expected, json);
        if (!result.isEmpty()) {
            Assert.fail("Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    public void runFixtureTests() throws IOException {

        String testFixture = "/jsonUtils/jsonUtils-removeRecursive.json";
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tests = (List<Map<String, Object>>) JsonUtils.classpathToObject(testFixture);

        for (Map<String, Object> testUnit : tests) {

            Object data = testUnit.get("input");
            String toRemove = (String) testUnit.get("remove");
            Object expected = testUnit.get("expected");

            JsonUtils.removeRecursive(data, toRemove);

            Diffy.Result result = diffy.diff(expected, data);
            if (!result.isEmpty()) {
                Assert.fail("Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
            }
        }
    }

    @Test
    public void validateJacksonClosesInputStreams() {

        final Set<String> closedSet = new HashSet<>();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream("{ \"a\" : \"b\" }".getBytes()) {
            @Override
            public void close() throws IOException {
                closedSet.add("closed");
                super.close();
            }
        };

        // Pass our wrapped InputStream to Jackson via JsonUtils.
        Map<String, Object> map = JsonUtils.jsonToMap(byteArrayInputStream);

        // Verify that we in fact loaded some data
        Assert.assertNotNull(map);
        Assert.assertEquals(1, map.size());

        // Verify that the close method was in fact called on the InputStream
        Assert.assertEquals(1, closedSet.size());
    }

    @DataProvider(parallel = true)
    public Iterator<Object[]> coordinates() throws IOException {
        List<Object[]> testCases = com.beust.jcommander.internal.Lists.newArrayList();

        testCases.add(new Object[]{0, new Object[]{"a", "b", 0}});
        testCases.add(new Object[]{1, new Object[]{"a", "b", 1}});
        testCases.add(new Object[]{2, new Object[]{"a", "b", 2}});
        testCases.add(new Object[]{1.618, new Object[]{"a", "b", 3}});
        testCases.add(new Object[]{"m", new Object[]{"p", 0}});
        testCases.add(new Object[]{"n", new Object[]{"p", 1}});
        testCases.add(new Object[]{1, new Object[]{"p", 2, "1"}});
        testCases.add(new Object[]{2, new Object[]{"p", 2, "2"}});
        testCases.add(new Object[]{3.14159, new Object[]{"p", 2, "pi"}});
        testCases.add(new Object[]{"y", new Object[]{"x"}});

        testCases.add(new Object[]{((Map<?, ?>) jsonSource).get("a"), new Object[]{"a"}});
        testCases.add(new Object[]{((Map<?, ?>) (((Map<?, ?>) jsonSource).get("a"))).get("b"), new Object[]{"a", "b"}});
        testCases.add(new Object[]{((List<?>) ((Map<?, ?>) (((Map<?, ?>) jsonSource).get("a"))).get("b")).get(0), new Object[]{"a", "b", 0}});
        testCases.add(new Object[]{((List<?>) ((Map<?, ?>) (((Map<?, ?>) jsonSource).get("a"))).get("b")).get(1), new Object[]{"a", "b", 1}});
        testCases.add(new Object[]{((List<?>) ((Map<?, ?>) (((Map<?, ?>) jsonSource).get("a"))).get("b")).get(2), new Object[]{"a", "b", 2}});
        testCases.add(new Object[]{((List<?>) ((Map<?, ?>) (((Map<?, ?>) jsonSource).get("a"))).get("b")).get(3), new Object[]{"a", "b", 3}});
        testCases.add(new Object[]{((Map<?, ?>) jsonSource).get("p"), new Object[]{"p"}});
        testCases.add(new Object[]{((List<?>) (((Map<?, ?>) jsonSource).get("p"))).get(0), new Object[]{"p", 0}});
        testCases.add(new Object[]{((List<?>) (((Map<?, ?>) jsonSource).get("p"))).get(1), new Object[]{"p", 1}});
        testCases.add(new Object[]{((List<?>) (((Map<?, ?>) jsonSource).get("p"))).get(2), new Object[]{"p", 2}});
        testCases.add(new Object[]{((Map<?, ?>) ((List<?>) (((Map<?, ?>) jsonSource).get("p"))).get(2)).get("1"), new Object[]{"p", 2, "1"}});
        testCases.add(new Object[]{((Map<?, ?>) ((List<?>) (((Map<?, ?>) jsonSource).get("p"))).get(2)).get("2"), new Object[]{"p", 2, "2"}});
        testCases.add(new Object[]{((Map<?, ?>) ((List<?>) (((Map<?, ?>) jsonSource).get("p"))).get(2)).get("pi"), new Object[]{"p", 2, "pi"}});
        testCases.add(new Object[]{((Map<?, ?>) jsonSource).get("x"), new Object[]{"x"}});

        return testCases.iterator();
    }

    /**
     * Method: navigate(Object source, Object... paths)
     */
    @Test(dataProvider = "coordinates")
    @SuppressWarnings("deprecation")
    public void navigator(Object expected, Object[] path) throws Exception {
        Object actual = JsonUtils.navigate(jsonSource, path);
        Assert.assertEquals(actual, expected);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testJsonToObjectWithInvalidCharsetThrowsException() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.jsonToObject("{\"a\":1}", "invalid-charset");
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testJsonToMapWithInvalidCharsetThrowsException() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.jsonToMap("{\"a\":1}", "invalid-charset");
    }

    @Test
    public void testJsonToListWithValidJson() {
        JsonUtilImpl util = new JsonUtilImpl();
        List<Object> result = util.jsonToList("[1, 2, 3]");
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), 1);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testJsonToListWithInvalidCharsetThrowsException() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.jsonToList("{\"a\":1}", "invalid-charset");
    }

    @Test
    public void testFilepathToObject_ValidFile() throws IOException {
        String path = Objects.requireNonNull(getClass().getResource("/jsonUtils/valid.json")).getFile();

        JsonUtilImpl util = new JsonUtilImpl();
        Object result = util.filepathToObject(path);
        Assert.assertTrue(result instanceof java.util.Map);
        Assert.assertEquals(((java.util.Map<?, ?>) result).get("foo"), 123);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFilepathToObject_FileNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.filepathToObject("non_existent_file.json");
    }

    @Test
    public void testFilepathToMap_ValidFile() throws IOException {
        String path = Objects.requireNonNull(getClass().getResource("/jsonUtils/valid.json")).getFile();
        JsonUtilImpl util = new JsonUtilImpl();
        Map<String, Object> result = util.filepathToMap(path);
        Assert.assertEquals(result.get("foo"), 123);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFilepathToMap_FileNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.filepathToMap("non_existent_map.json");
    }

    @Test
    public void testFilepathToList_ValidFile() throws IOException {
        String path = Objects.requireNonNull(getClass().getResource("/jsonUtils/valid_list.json")).getFile();
        JsonUtilImpl util = new JsonUtilImpl();
        List<Object> result = util.filepathToList(path);
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), 1);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFilepathToList_FileNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.filepathToList("non_existent_list.json");
    }

    @Test
    public void testClasspathToObject_ValidResource() {
        JsonUtilImpl util = new JsonUtilImpl();
        Object result = util.classpathToObject("/jsonUtils/valid.json");
        Assert.assertTrue(result instanceof Map);
        Assert.assertEquals(((Map<?, ?>) result).get("foo"), 123);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testClasspathToObject_ResourceNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.classpathToObject("/jsonUtils/non_existent.json");
    }

    @Test
    public void testClasspathToMap_ValidResource() {
        JsonUtilImpl util = new JsonUtilImpl();
        Map<String, Object> result = util.classpathToMap("/jsonUtils/valid.json");
        Assert.assertEquals(result.get("foo"), 123);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testClasspathToMap_ResourceNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.classpathToMap("/jsonUtils/non_existent.json");
    }

    @Test
    public void testClasspathToList_ValidResource() {
        JsonUtilImpl util = new JsonUtilImpl();
        List<Object> result = util.classpathToList("/jsonUtils/valid_list.json");
        Assert.assertEquals(result.size(), 3);
        Assert.assertEquals(result.get(0), 1);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testClasspathToList_ResourceNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.classpathToList("/jsonUtils/non_existent_list.json");
    }

    @Test
    public void testFileToType_WithTypeReference_ValidFile() throws IOException {
        String path = Objects.requireNonNull(getClass().getResource("/jsonUtils/valid.json")).getFile();

        JsonUtilImpl util = new JsonUtilImpl();
        Map<String, Integer> result = util.fileToType(
                path,
                new TypeReference<Map<String, Integer>>() {}
        );
        Assert.assertEquals(result.get("foo"), 123);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFileToType_WithTypeReference_FileNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.fileToType("non_existent_type_ref.json", new TypeReference<Map<String, String>>() {});
    }

    @Test
    public void testFileToType_WithClass_ValidFile() throws IOException {
        String path = Objects.requireNonNull(getClass().getResource("/jsonUtils/valid.json")).getFile();
        JsonUtilImpl util = new JsonUtilImpl();
        Map result = util.fileToType(path, Map.class);
        Assert.assertEquals(result.get("foo"), 123);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void testFileToType_WithClass_FileNotFound() {
        JsonUtilImpl util = new JsonUtilImpl();
        util.fileToType("non_existent_class.json", Map.class);
    }
}
