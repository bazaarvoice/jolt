/*
 * Copyright 2015 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt.shiftr.spec;

import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class RHSParsingTest {

    @DataProvider
    public Object[][] RHSParsingTests() throws IOException {
        return new Object[][] {
            {
                "simple, no escape",
                "a.b.c",
                Arrays.asList( "a", "b", "c" ),
            },
            {
                "ref and array, no escape",
                "a.&(1,2).[]",
                Arrays.asList( "a", "&(1,2)", "[]" )
            },
            {
                "single transpose, no escape",
                "a.@(l.m.n).c",
                Arrays.asList( "a", "@(l.m.n)", "c" )
            },
            {
                "non-special char escape passes thru",
                "a\\\\bc.def",
                Arrays.asList( "a\\bc", "def" )
            },
            {
                "single escape",
                "a\\.b.c",
                Arrays.asList( "a.b", "c" )
            },
            {
                "double escape in front of a period, does not escape the period",
                "a\\\\.b.c",
                Arrays.asList( "a\\", "b", "c" )
            },
            {
                "@Class example",
                "a.@Class.c",
                Arrays.asList( "a", "@(Class)", "c" )
            }
        };
    }

    @Test(dataProvider = "RHSParsingTests" )
    public void testRHSParsing( String testName, String unSweetendDotNotation, List<String> expected ) {

        List<String> actual = ShiftrSpec.parseDotNotation( Lists.<String>newArrayList(), ShiftrSpec.stringIterator(unSweetendDotNotation), unSweetendDotNotation );

        Assert.assertEquals( actual, expected, "Failed test name " + testName );
    }
}
