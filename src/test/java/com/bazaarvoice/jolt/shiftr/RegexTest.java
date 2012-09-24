package com.bazaarvoice.jolt.shiftr;

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

        Matcher matcher = pattern.matcher( "&3" );

        if ( matcher.find() ) {
            String pathRef = matcher.group(1);
            String keyRef = matcher.group(3);
            AssertJUnit.assertEquals( "3", pathRef );
            AssertJUnit.assertNull( keyRef );
        }
        else {
            AssertJUnit.fail("should have found a match");
        }

        matcher = pattern.matcher( "&1(2)" );

        if ( matcher.find()) {
            String pathRef = matcher.group(1);
            String keyRef = matcher.group(3);
            AssertJUnit.assertEquals( "1", pathRef );
            AssertJUnit.assertEquals( "2", keyRef );
        }
        else {
            AssertJUnit.fail("should have found a match");
        }

        matcher = pattern.matcher( "&" );

        if ( matcher.find()) {
            String pathRef = matcher.group(1);
            String keyRef = matcher.group(3);
            AssertJUnit.assertNull( pathRef );
            AssertJUnit.assertNull( keyRef );
        }
        else {
            AssertJUnit.fail("should have found a match");
        }
    }
}
