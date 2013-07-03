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
import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
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
                    new ArrayList<Object>(),
            },
            {
                    "Empty spec",
                    JsonUtils.jsonToMap( "{ }" ),
            },
            {
                    "Empty sub-spec",
                    JsonUtils.jsonToMap( "{ \"tuna\" : {} }" ),
            },
            {
                    "Bad @",
                    JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-@\" : \"&(1,2).&.value\" } }" ),
            },
            {
                    "RHS @",
                    JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).@.value\" } }" ),
            },
            {
                    "RHS *",
                    JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).*.value\" } }" ),
            },
            {
                    "RHS $",
                    JsonUtils.jsonToMap( "{ \"tuna-*-marlin-*\" : { \"rating-*\" : \"&(1,2).$.value\" } }" ),
            },
            {
                    "Two Arrays",
                    JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-*\" : [ \"&(1,2).photos[&(0,1)]-subArray[&(1,2)].value\", \"foo\"] } }"),
            },
            {
                    "Can't mix * and & in the same key",
                    JsonUtils.jsonToMap("{ \"tuna-*-marlin-*\" : { \"rating-&(1,2)-*\" : [ \"&(1,2).value\", \"foo\"] } }"),
            }
        };
    }

    @Test(dataProvider = "badSpecs", expectedExceptions = SpecException.class)
    public void failureUnitTest(String testName, Object spec) {
        new Shiftr( spec );
    }
}
