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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JsonUtilsTest {

    private Diffy diffy = new Diffy();

    private Map ab = ImmutableMap.builder().put( "a", "b" ).build();
    private Map cd = ImmutableMap.builder().put( "c", "d" ).build();
    private Map top = ImmutableMap.builder().put( "A", ab ).put( "B", cd ).build();

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

        JsonUtils.removeRecursive( json, key );

        Diffy.Result result = diffy.diff( expected, json );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }

    @Test
    public void runFixtureTests() throws IOException {

        String testFixture = "/jsonUtils/jsonUtils-removeRecursive.json";
        List<Map<String, Object>> tests = (List<Map<String, Object>>) JsonUtils.classpathToObject( testFixture );

        for ( Map<String,Object> testUnit : tests ) {

            Object data = testUnit.get( "input" );
            String toRemove = (String) testUnit.get( "remove" );
            Object expected = testUnit.get( "expected" );

            JsonUtils.removeRecursive( data, toRemove );

            Diffy.Result result = diffy.diff( expected, data );
            if (!result.isEmpty()) {
                AssertJUnit.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
            }
        }
    }

    @Test( description = "No exception if we don't try to remove from an ImmutableMap.")
    public void doNotUnnecessarilyDieOnImmutableMaps() throws IOException
    {
        Map expected = JsonUtils.jsonToMap( JsonUtils.toJsonString( top ) );

        JsonUtils.removeRecursive( top, "tuna" );

        Diffy.Result result = diffy.diff( expected, top );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }

    @Test( expectedExceptions = UnsupportedOperationException.class, description = "Exception if try to remove from an Immutable map.")
    public void correctExceptionWithImmutableMap() throws IOException
    {
        JsonUtils.removeRecursive( top, "c" );
    }

    @Test
    public void validateJacksonClosesInputStreams() {

        final Set<String> closedSet = new HashSet<String>();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( "{ \"a\" : \"b\" }".getBytes() ) {
            @Override
            public void close() throws IOException {
                closedSet.add("closed");
                super.close();
            }
        };

        // Pass our wrapped InputStream to Jackson via JsonUtils.
        Map<String,Object> map = JsonUtils.jsonToMap( byteArrayInputStream );

        // Verify that we in fact loaded some data
        AssertJUnit.assertNotNull( map );
        AssertJUnit.assertEquals( 1, map.size() );

        // Verify that the close method was in fact called on the InputStream
        AssertJUnit.assertEquals( 1, closedSet.size() );
    }
}
