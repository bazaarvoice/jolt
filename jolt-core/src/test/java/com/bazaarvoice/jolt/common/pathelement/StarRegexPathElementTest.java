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
package com.bazaarvoice.jolt.common.pathelement;

import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StarRegexPathElementTest {

    @DataProvider
    public Object[][] getStarPatternTests() {
        return new Object[][] {
                {"easy star test",                "rating-*-*",               "rating-tuna-marlin",                         "tuna",      "marlin"},
                {"easy facet usage",              "terms--config--*--*--cdv", "terms--config--Expertise--12345--cdv",       "Expertise", "12345"},
                {"degenerate ProductId in facet", "terms--config--*--*--cdv", "terms--config--Expertise--12345--6789--cdv", "Expertise", "12345--6789"},
        };
    }

    @Test( dataProvider = "getStarPatternTests")
    public void starPatternTest( String testName, String spec, String dataKey, String expected1, String expected2 ) {

        StarPathElement star = new StarRegexPathElement( spec );

        LiteralPathElement lpe = star.match( dataKey, null );

        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );
        AssertJUnit.assertEquals( dataKey,   lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( expected1, lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( expected2, lpe.getSubKeyRef( 2 ) );
    }

    @Test
    public void mustMatchSomethingTest() {

        StarPathElement star = new StarRegexPathElement( "tuna-*-*");

        AssertJUnit.assertNull( star.match( "tuna--", null ) );
        AssertJUnit.assertNull( star.match( "tuna-bob-", null ) );
        AssertJUnit.assertNull( star.match( "tuna--bob", null ) );
    }
}
