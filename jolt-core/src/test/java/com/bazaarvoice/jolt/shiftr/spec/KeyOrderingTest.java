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
package com.bazaarvoice.jolt.shiftr.spec;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.SpecDriven;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KeyOrderingTest {

    @DataProvider
    public Object[][] shiftrKeyOrderingTestCases() throws IOException {
        return new Object[][] {
            {
                "Simple * and &",
                JsonUtils.jsonToMap( "{ \"*\" : { \"a\" : \"b\" }, \"&\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "&(0,0)", "*" )
            },
            {
                "2* and 2&",
                JsonUtils.jsonToMap( "{ \"rating-*\" : { \"a\" : \"b\" }, \"rating-range-*\" : { \"a\" : \"b\" }, \"&\" : { \"a\" : \"b\" }, \"tuna-&(0)\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "tuna-&(0,0)", "&(0,0)", "rating-range-*", "rating-*" )
            },
            {
                "2& alpha-number based fallback",
                JsonUtils.jsonToMap( "{ \"&\" : { \"a\" : \"b\" }, \"&(0,1)\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "&(0,0)", "&(0,1)" )
            },
            {
                "2* and 2& alpha fallback",
                JsonUtils.jsonToMap( "{ \"aaaa-*\" : { \"a\" : \"b\" }, \"bbbb-*\" : { \"a\" : \"b\" }, \"aaaa-&\" : { \"a\" : \"b\" }, \"bbbb-&(0)\" : { \"a\" : \"b\" } }" ),
                Arrays.asList( "aaaa-&(0,0)", "bbbb-&(0,0)", "aaaa-*", "bbbb-*" )
            }
        };
    }

    @Test(dataProvider = "shiftrKeyOrderingTestCases" )
    public void testKeyOrdering( String testName, Map<String,Object> spec, List<String> expectedOrder ) {

        ShiftrCompositeSpec root = new ShiftrCompositeSpec( SpecDriven.ROOT_KEY, spec );

        for ( int index = 0; index < expectedOrder.size(); index++) {
            String expected = expectedOrder.get( index );
            Assert.assertEquals( expected, root.getComputedChildren().get( index ).pathElement.getCanonicalForm(), testName );
        }
    }
}
