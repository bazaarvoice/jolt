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

import com.bazaarvoice.jolt.common.tree.MatchedElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class StarRegexPathElementTest {

    @DataProvider
    public Object[][] getStarPatternTests() {
        return new Object[][] {
                {"easy star test",                "rating-*-*",               "rating-tuna-marlin",                         "tuna",      "marlin"},
                {"easy facet usage",              "terms--config--*--*--cdv", "terms--config--Expertise--12345--cdv",       "Expertise", "12345"},
                {"degenerate ProductId in facet", "terms--config--*--*--cdv", "terms--config--Expertise--12345--6789--cdv", "Expertise", "12345--6789"},
                {"multi metachar test",           "rating.$.*.*",               "rating.$.marlin$.test.",                   "marlin$",   "test."},
        };
    }

    @Test( dataProvider = "getStarPatternTests")
    public void starPatternTest( String testName, String spec, String dataKey, String expected1, String expected2 ) {

        StarPathElement star = new StarRegexPathElement( spec );

        MatchedElement lpe = star.match( dataKey, null );

        Assert.assertEquals( 3, lpe.getSubKeyCount() );
        Assert.assertEquals( dataKey,   lpe.getSubKeyRef( 0 ) );
        Assert.assertEquals( expected1, lpe.getSubKeyRef( 1 ) );
        Assert.assertEquals( expected2, lpe.getSubKeyRef( 2 ) );
    }

    @Test
    public void mustMatchSomethingTest() {

        StarPathElement star = new StarRegexPathElement( "tuna-*-*");

        Assert.assertNull( star.match( "tuna--", null ) );
        Assert.assertNull( star.match( "tuna-bob-", null ) );
        Assert.assertNull( star.match( "tuna--bob", null ) );

        StarPathElement multiMetacharStarpathelement = new StarRegexPathElement( "rating-$-*-*");

        Assert.assertNull( multiMetacharStarpathelement.match( "rating-capGrp1-capGrp2", null ) );
        Assert.assertNull( multiMetacharStarpathelement.match( "rating-$capGrp1-capGrp2", null ) );
        Assert.assertNotNull(multiMetacharStarpathelement.match( "rating-$-capGrp1-capGrp2",null) );
    }
}
