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
import com.bazaarvoice.jolt.modifier.function.Objects;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JoltUtilsSquashTest {

    private Diffy diffy = new Diffy();

    @Test
    public void squashNullsInAListTest() {
        List actual = new ArrayList();
        actual.addAll( Arrays.asList( "a", null, 1, null, "b", 2) );

        List expectedList = Arrays.asList( "a", 1, "b", 2);

        Objects.squashNulls( actual );

        Diffy.Result result = diffy.diff( expectedList, actual );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }

    @Test
    public void squashNullsInAMapTest() {
        Map<String,Object> actual = new HashMap<>();
        actual.put( "a", 1 );
        actual.put( "b", null );
        actual.put( "c", "C" );

        Map<String,Object>  expectedMap = new HashMap<>();
        expectedMap.put( "a",  1  );
        expectedMap.put( "c", "C" );

        Objects.squashNulls( actual );

        Diffy.Result result = diffy.diff( expectedMap, actual );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }


    @Test
    public void recursivelySquashNullsTest()
    {
        Map<String,Object> actual   = JsonUtils.javason( "{ 'a' : 1, 'b' : null, 'c' : [ null, 4, null, 5, { 'x' : 'X', 'y' : null } ] }" );
        Map<String,Object> expected = JsonUtils.javason( "{ 'a' : 1,             'c' : [       4,       5, { 'x' : 'X'             } ] }" );

        Objects.recursivelySquashNulls( actual );

        Diffy.Result result = diffy.diff( expected, actual );
        if (!result.isEmpty()) {
            Assert.fail( "Failed.\nhere is a diff:\nexpected: " + JsonUtils.toJsonString( result.expected ) + "\n  actual: " + JsonUtils.toJsonString( result.actual ) );
        }
    }
}