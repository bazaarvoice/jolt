package com.bazaarvoice.jolt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.io.IOException;
import java.util.Map;

public class JsonUtilsTest {

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
    public void testRemoveRecursive(Object json, String key, Object expected)
            throws IOException {
        JsonUtils.removeRecursive( json, key );
        Diffy.Result result = new Diffy().diff( expected, json );
        if (!result.isEmpty()) {
            AssertJUnit.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }
}
