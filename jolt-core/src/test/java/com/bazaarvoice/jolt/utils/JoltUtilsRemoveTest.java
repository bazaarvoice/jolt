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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.List;
import java.util.Map;

public class JoltUtilsRemoveTest {

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
    public void testRemoveRecursive(Object json, String key, Object expected) {

        JoltUtils.removeRecursive( json, key );

        Diffy.Result result = diffy.diff( expected, json );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }

    @Test
    public void runFixtureTests() {

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
    public void doNotUnnecessarilyDieOnImmutableMaps()
    {
        Map expected = JsonUtils.jsonToMap( JsonUtils.toJsonString( top ) );

        JoltUtils.removeRecursive( top, "tuna" );

        Diffy.Result result = diffy.diff( expected, top );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString(result.expected) + "\n  actual: " + JsonUtils.toJsonString(result.actual));
        }
    }

    @Test( expectedExceptions = UnsupportedOperationException.class, description = "Exception if try to remove from an Immutable map.")
    public void correctExceptionWithImmutableMap()
    {
        JoltUtils.removeRecursive( top, "c" );
    }
}