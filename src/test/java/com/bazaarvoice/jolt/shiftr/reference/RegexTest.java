package com.bazaarvoice.jolt.shiftr.reference;

import com.bazaarvoice.jolt.shiftr.pathelement.StarPathElement;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    @DataProvider
    public Object[][] getStarPatternTests() {
        return new Object[][] {
            { "easy star test", "rating-*-*", "rating-tuna-marlin", "tuna", "marlin" },

            { "easy facet usage"             ,"terms--config--*--*--cdv", "terms--config--Expertise--12345--cdv",       "Expertise", "12345" },
            { "degenerate ProductId in facet","terms--config--*--*--cdv", "terms--config--Expertise--12345--6789--cdv", "Expertise", "12345--6789" },
        };
    }

    @Test( dataProvider = "getStarPatternTests")
    public void starPatternTest( String testName, String spec, String dataKey, String expected1, String expected2 ) {

        Pattern pattern = StarPathElement.makePattern( spec );
        Matcher matcher = pattern.matcher( dataKey );

        if ( matcher.find() ) {
            AssertJUnit.assertEquals( testName + " - 1st star", expected1, matcher.group(1) );
            AssertJUnit.assertEquals( testName + " - 2nd star", expected2, matcher.group(2) );
        }
    }

    @Test
    public void ampReferencePatternTest() {
        Pattern pattern = AmpReference.refPattern;

        {
            Matcher matcher = pattern.matcher( "&3" );
            if ( matcher.find() ) {
                AssertJUnit.assertEquals( "3", matcher.group(1) );
                AssertJUnit.assertNull( matcher.group(2) );
                AssertJUnit.assertNull( matcher.group(3) );
                AssertJUnit.assertNull( matcher.group(4) );
                AssertJUnit.assertNull( matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }
        {
            Matcher matcher = pattern.matcher( "&(3)" );
            if ( matcher.find() ) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertEquals( "(3)", matcher.group(2) );
                AssertJUnit.assertEquals( "3", matcher.group(3) );
                AssertJUnit.assertNull( matcher.group(4) );
                AssertJUnit.assertNull( matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }

        {
            Matcher matcher = pattern.matcher( "&(1,2)" );
            if ( matcher.find()) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertEquals( "(1,2)", matcher.group(2) );
                AssertJUnit.assertEquals( "1", matcher.group(3) );
                AssertJUnit.assertEquals( ",2", matcher.group(4) );
                AssertJUnit.assertEquals( "2", matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }

        {
            Matcher matcher = pattern.matcher( "&(1,2)" );
            if ( matcher.find()) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertEquals( "(1,2)", matcher.group(2) );
                AssertJUnit.assertEquals( "1", matcher.group(3) );
                AssertJUnit.assertEquals( ",2", matcher.group(4) );
                AssertJUnit.assertEquals( "2", matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }

        {
            Matcher matcher = pattern.matcher( "&" );
            if ( matcher.find()) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertNull( matcher.group(2) );
                AssertJUnit.assertNull( matcher.group(3) );
                AssertJUnit.assertNull( matcher.group(4) );
                AssertJUnit.assertNull( matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }
    }

    @Test
    public void dollarReferencePatternTest() {
        Pattern pattern = DollarReference.refPattern;

        {
            Matcher matcher = pattern.matcher( "$3" );
            if ( matcher.find() ) {
                AssertJUnit.assertEquals( "3", matcher.group(1) );
                AssertJUnit.assertNull( matcher.group(2) );
                AssertJUnit.assertNull( matcher.group(3) );
                AssertJUnit.assertNull( matcher.group(4) );
                AssertJUnit.assertNull( matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }
        {
            Matcher matcher = pattern.matcher( "$(3)" );
            if ( matcher.find() ) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertEquals( "(3)", matcher.group(2) );
                AssertJUnit.assertEquals( "3", matcher.group(3) );
                AssertJUnit.assertNull( matcher.group(4) );
                AssertJUnit.assertNull( matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }

        {
            Matcher matcher = pattern.matcher( "$(1,2)" );
            if ( matcher.find()) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertEquals( "(1,2)", matcher.group(2) );
                AssertJUnit.assertEquals( "1", matcher.group(3) );
                AssertJUnit.assertEquals( ",2", matcher.group(4) );
                AssertJUnit.assertEquals( "2", matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }

        {
            Matcher matcher = pattern.matcher( "$(1,2)" );
            if ( matcher.find()) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertEquals( "(1,2)", matcher.group(2) );
                AssertJUnit.assertEquals( "1", matcher.group(3) );
                AssertJUnit.assertEquals( ",2", matcher.group(4) );
                AssertJUnit.assertEquals( "2", matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }

        {
            Matcher matcher = pattern.matcher( "$" );
            if ( matcher.find()) {
                AssertJUnit.assertNull( matcher.group(1) );
                AssertJUnit.assertNull( matcher.group(2) );
                AssertJUnit.assertNull( matcher.group(3) );
                AssertJUnit.assertNull( matcher.group(4) );
                AssertJUnit.assertNull( matcher.group(5) );
            }
            else {
                AssertJUnit.fail("should have found a match");
            }
        }
    }
}
