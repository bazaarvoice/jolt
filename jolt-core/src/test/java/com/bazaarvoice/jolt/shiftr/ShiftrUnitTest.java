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
package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.JoltTestUtil;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.common.PathElementBuilder;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.pathelement.TransposePathElement;
import com.bazaarvoice.jolt.exception.SpecException;
import com.google.common.base.Joiner;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShiftrUnitTest {

    @DataProvider
    public Object[][] shiftrTestCases() throws IOException {
        return new Object[][] {
            {
                "Simple * and Reference",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).&.value\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"bar\" } }"),
                JsonUtils.jsonToMap("{ \"AAA\" : { \"rating-BBB\" : { \"value\" : \"bar\" } } }")
            },
            {
                "Shift to two places",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : [ \"&(1,2).&.value\", \"foo\"] } }"),
                JsonUtils.jsonToMap("{ \"tuna-A-marlin-AAA\" : { \"rating-BBB\" : \"bar\" } }"),
                JsonUtils.jsonToMap("{ \"foo\" : \"bar\", \"AAA\" : { \"rating-BBB\" : { \"value\" : \"bar\" } } }")
            },
            {
                "Or",
                JsonUtils.jsonToMap("{ \"tuna|marlin\" : \"&-write\" }"),
                JsonUtils.jsonToMap("{ \"tuna\" : \"snapper\" }"),
                JsonUtils.jsonToMap("{ \"tuna-write\" : \"snapper\" }")
            },
            {
                "KeyRef",
                JsonUtils.jsonToMap("{ \"rating-*\" : { \"&(0,1)\" : { \"match\" : \"&\" } } }"),
                JsonUtils.jsonToMap("{ \"rating-a\" : { \"a\" : { \"match\": \"a-match\" }, \"random\" : { \"match\" : \"noise\" } }," +
                        "              \"rating-c\" : { \"c\" : { \"match\": \"c-match\" }, \"random\" : { \"match\" : \"noise\" } } }"),
                JsonUtils.jsonToMap("{ \"match\" : [ \"a-match\", \"c-match\" ] }")
            },
            {
                "Complex array write",
                JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"tuna[&(1,1)].marlin[&(1,2)].&(0,1)\" } }"),
                JsonUtils.jsonToMap("{ \"tuna-2-marlin-3\" : { \"rating-BBB\" : \"bar\" }," +
                                      "\"tuna-1-marlin-0\" : { \"rating-AAA\" : \"mahi\" } }"),
                JsonUtils.jsonToMap("{ \"tuna\" : [ null, " +
                        "                           { \"marlin\" : [ { \"AAA\" : \"mahi\" } ] }, " +
                        "                           { \"marlin\" : [ null, null, null, { \"BBB\" : \"bar\" } ] } " +
                        "                         ] " +
                        "            }")
            }
        };
    }

    @Test(dataProvider = "shiftrTestCases")
    public void shiftrUnitTest(String testName, Map<String, Object> spec, Map<String, Object> data, Map<String, Object> expected) throws Exception {

        Shiftr shiftr = new Shiftr( spec );
        Object actual = shiftr.transform( data );

        JoltTestUtil.runDiffy( testName, expected, actual );
    }


    @DataProvider
    public Object[][] badSpecs() throws IOException {
        return new Object[][] {
            {
                    "Null Spec",
                    null,
            },
            {
                    "List Spec",
                    new ArrayList<>(),
            },
            {
                    "Empty spec",
                    JsonUtils.jsonToMap( "{ }" ),
            },
            {
                    "Empty sub-spec",
                    JsonUtils.javason( "{ 'tuna' : {} }" ),
            },
            {
                    "Bad @",
                    JsonUtils.javason( "{ 'tuna-*-marlin-*' : { 'rating-@' : '&(1,2).&.value' } }" ),
            },
            {
                    "RHS @ by itself",
                    JsonUtils.javason( "{ 'tuna-*-marlin-*' : { 'rating-*' : '&(1,2).@.value' } }" ),
            },
            {
                    "RHS @ with bad Parens",
                    JsonUtils.javason( "{ 'tuna-*-marlin-*' : { 'rating-*' : '&(1,2).@(data.&(1,1).value' } }" ),
            },
            {
                    "RHS *",
                    JsonUtils.javason( "{ 'tuna-*-marlin-*' : { 'rating-*' : '&(1,2).*.value' } }" ),
            },
            {
                    "RHS $",
                    JsonUtils.javason( "{ 'tuna-*-marlin-*' : { 'rating-*' : '&(1,2).$.value' } }" ),
            },
            {
                    "Two Arrays",
                    JsonUtils.javason("{ 'tuna-*-marlin-*' : { 'rating-*' : [ '&(1,2).photos[&(0,1)]-subArray[&(1,2)].value', 'foo'] } }"),
            },
            {
                    "Can't mix * and & in the same key",
                    JsonUtils.javason("{ 'tuna-*-marlin-*' : { 'rating-&(1,2)-*' : [ '&(1,2).value', 'foo'] } }"),
            },
            {
                    "Don't put negative numbers in array references",
                    JsonUtils.javason("{ 'tuna' : 'marlin[-1]' }"),
            }
        };
    }

    @Test(dataProvider = "badSpecs", expectedExceptions = SpecException.class)
    public void failureUnitTest(String testName, Object spec) {
        new Shiftr( spec );
    }

    /**
     * @return canonical dotNotation String built from the given paths
     */
    public String buildCanonicalString( List<PathElement> paths ) {

        List<String> pathStrs = new ArrayList<>( paths.size() );
        for( PathElement pe : paths ) {
            pathStrs.add( pe.getCanonicalForm() );
        }

        return Joiner.on(".").join( pathStrs );
    }


    @DataProvider
    public Object[][] validRHS() throws IOException {
        return new Object[][]{
            { "@a", "@(0,a)" },
            { "@abc", "@(0,abc)" },
            { "@a.b.c", "@(0,a).b.c" },
            { "@(a.b\\.c)", "@(0,a.b\\.c)" },
            { "@a.b.c", "@(0,a).b.c" },
            { "@a.b.@c", "@(0,a).b.@(0,c)" },
            { "@(a[2].&).b.@c", "@(0,a.[2].&(0,0)).b.@(0,c)" },
            { "a[&2].@b[1].c", "a.[&(2,0)].@(0,b).[1].c" }
        };
    }

    @Test(dataProvider = "validRHS" )
    public void validRHSTests( String dotNotation, String expected ) {
        List<PathElement> paths = PathElementBuilder.parseDotNotationRHS( dotNotation );
        String actualCanonicalForm = buildCanonicalString( paths );

        Assert.assertEquals( actualCanonicalForm, expected, "TestCase: " + dotNotation );
    }

    @Test
    public void testTransposePathParsing() {

        List<PathElement> paths = PathElementBuilder.parseDotNotationRHS( "test.@(2,foo\\.bar)" );

        Assert.assertEquals( paths.size(), 2 );
        TransposePathElement actualApe = (TransposePathElement) paths.get( 1 );

        Assert.assertEquals( actualApe.getCanonicalForm(), "@(2,foo\\.bar)" );
    }

    @DataProvider
    public Object[][] badRHS() throws IOException {
        return new Object[][]{
                { "@" },
                { "a@" },
                { "@a@b" },
                { "@(a.b.&(2,2)" }, // missing trailing )
                { "@(a.b.&(2,2).d" }, // missing trailing )
                { "@(a.b.@c).d" },
                { "@(a.*.c)" }, // @ can not contain a *
                { "@(a.$2.c)" }, // @ can not contain a $
        };
    }

    @Test(dataProvider = "badRHS", expectedExceptions = SpecException.class)
    public void failureRHSTests( String dotNotation ) {
        PathElementBuilder.parseDotNotationRHS( dotNotation );
    }
}
