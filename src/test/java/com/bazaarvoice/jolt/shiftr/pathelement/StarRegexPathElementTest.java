package com.bazaarvoice.jolt.shiftr.pathelement;

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