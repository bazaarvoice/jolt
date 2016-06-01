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

import com.bazaarvoice.jolt.common.SpecStringParser;
import com.google.common.collect.Lists;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SpecParsingTest {

    @DataProvider
    public Object[][] RHSParsingTestsRemoveEscapes() throws IOException {
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
                "escaping rhs",
                "data.\\\\$rating-&1",
                Arrays.asList( "data", "\\$rating-&1" )
            },
            {
                "@Class example",
                "a.@Class.c",
                Arrays.asList( "a", "@(Class)", "c" )
            }
        };
    }

    @Test(dataProvider = "RHSParsingTestsRemoveEscapes")
    public void testRHSParsingRemoveEscapes( String testName, String unSweetendDotNotation, List<String> expected ) {

        List<String> actual = SpecStringParser.parseDotNotation( Lists.<String>newArrayList(), SpecStringParser.stringIterator( unSweetendDotNotation ), unSweetendDotNotation );

        Assert.assertEquals( actual, expected, "Failed test name " + testName );
    }

    @DataProvider
    public Object[][] removeEscapeCharsTests() throws IOException {

        return new Object[][] {
            { "starts with escape",     "\\@pants", "@pants" },
            { "escape in the middle",   "rating-\\&pants", "rating-&pants" },
            { "escape the escape char", "rating\\\\pants", "rating\\pants" },
        };
    }

    @Test(dataProvider = "removeEscapeCharsTests" )
    public void testRemoveEscapeChars( String testName, String input, String expected ) {

        String actual = SpecStringParser.removeEscapeChars( input );
        Assert.assertEquals( actual, expected, "Failed test name " + testName );
    }


    @DataProvider
    public Object[][] removeEscapedValuesTest() throws IOException {

        return new Object[][] {
            { "starts with escape",     "\\@pants", "pants" },
            { "escape in the middle",   "rating-\\&pants", "rating-pants" },
            { "escape the escape char", "rating\\\\pants", "ratingpants" },
            { "escape the array", "\\[\\]pants", "pants" },
        };
    }

    @Test(dataProvider = "removeEscapedValuesTest" )
    public void testEscapeParsing( String testName, String input, String expected ) {

        String actual = SpecStringParser.removeEscapedValues( input );
        Assert.assertEquals( actual, expected, "Failed test name " + testName );
    }
}
