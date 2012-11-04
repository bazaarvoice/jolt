package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.Placr;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    @Test
    public void starPatternTest() {

        // rating-*-* -> rating-tuna-marlin should be [tuna, marlin]

        String start = "rating-*-*";
        String regex = "^" + start.replace("*", "(.*?)")  + "$";

        //Pattern pattern = Pattern.compile( "^rating-(.*?)-(.*?)$" );
        Pattern pattern = Pattern.compile( regex );

        Matcher matcher = pattern.matcher( "rating-tuna-marlin" );

        if ( matcher.find() ) {
            AssertJUnit.assertEquals( "tuna", matcher.group(1) );
            AssertJUnit.assertEquals( "marlin", matcher.group(2) );
        }
    }

    @Test
    public void referencePatternTest() {
        Pattern pattern = Reference.refPattern;

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
}
