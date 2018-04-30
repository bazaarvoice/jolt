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

import com.bazaarvoice.jolt.JsonUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static com.bazaarvoice.jolt.utils.JoltUtils.navigate;
import static com.bazaarvoice.jolt.utils.JoltUtils.navigateOrDefault;
import static com.bazaarvoice.jolt.utils.JoltUtils.navigateStrict;

public class JoltUtilsNavigateTest {

    private Object jsonSource;
    private Object jsonSource_empty;

    @BeforeClass
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


    @DataProvider (parallel = true)
    public Object[][]  validNavigateTests() {

        return new Object[][] {

            { 0, new Object[] {"a", "b", 0}},
            { 1, new Object[] {"a", "b", 1}},
            { 2, new Object[] {"a", "b", 2}},
            { 1.618, new Object[] {"a", "b", 3}},
            { "m", new Object[] {"p", 0} },
            { "n", new Object[] {"p", 1}},
            { 1, new Object[] {"p", 2, "1"}},
            { 2, new Object[] {"p", 2, "2"}},
            { 3.14159, new Object[] {"p", 2, "pi"}},
            { "y", new Object[] {"x"}},

            { ((Map) jsonSource).get("a"), new Object[] {"a"}},
            { ((Map)(((Map) jsonSource).get("a"))).get("b"), new Object[] {"a", "b"}},
            { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(0), new Object[] {"a", "b", 0}},
            { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(1), new Object[] {"a", "b", 1}},
            { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(2), new Object[] {"a", "b", 2}},
            { ((List)((Map)(((Map) jsonSource).get("a"))).get("b")).get(3), new Object[] {"a", "b", 3}},
            { ((Map) jsonSource).get("p"), new Object[] {"p"}},
            { ((List)(((Map) jsonSource).get("p"))).get(0), new Object[] {"p", 0}},
            { ((List)(((Map) jsonSource).get("p"))).get(1), new Object[] {"p", 1}},
            { ((List)(((Map) jsonSource).get("p"))).get(2), new Object[] {"p", 2}},
            { ((Map)((List)(((Map) jsonSource).get("p"))).get(2)).get("1"), new Object[] {"p", 2, "1"}},
            { ((Map)((List)(((Map) jsonSource).get("p"))).get(2)).get("2"), new Object[] {"p", 2, "2"}},
            { ((Map)((List)(((Map) jsonSource).get("p"))).get(2)).get("pi"), new Object[] {"p", 2, "pi"}},

            { ((Map) jsonSource).get("x"), new Object[] {"x"} },
        };
    }

    @Test (dataProvider = "validNavigateTests" )
    public void navigate_happy_tests(Object expected, Object[] path) {
        Object actual = navigate(jsonSource, path);
        Assert.assertEquals(actual, expected);
    }

    @Test (dataProvider = "validNavigateTests" )
    public void navigateStrict_happy_tests(Object expected, Object[] path) {
        Object actual = navigateStrict(jsonSource, path);
        Assert.assertEquals(actual, expected);
    }

    @Test (dataProvider = "validNavigateTests" )
    public void navigateOrDefault_happy_tests(Object expected, Object[] path) {
        Object actual = navigateOrDefault(null, jsonSource, path);
        Assert.assertEquals(actual, expected);
    }



    @Test( expectedExceptions = UnsupportedOperationException.class )
    public void navigateStrictThrowsException() {
        Object actual = navigateStrict(jsonSource, "pants", "shoes");
        Assert.fail( "Should have thrown an Exception" );
    }


    @DataProvider (parallel = true)
    public Object[][] navigateOrDefault_testCases() {

        return new Object[][] {

            { new Object[] {"a", "b" }}, // verify that trying to read from two nested that don't exist works
            { new Object[] {"h", -3 }},  // verify that trying to read from an existing list with a negative index does not blow up
            { new Object[] {"h", 4 }},   // verify that trying to read from an existing list with a index bigger that the list does not blow up
        };
    }

    @Test (dataProvider = "navigateOrDefault_testCases" )
    public void navigatorSafe(Object[] path) {

        Object actual = navigateOrDefault( "pants", jsonSource_empty, path);
        Assert.assertEquals(actual, "pants");
    }
}