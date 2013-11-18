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
import org.testng.annotations.Test;

public class StarDoublePathElementTest {

    @Test
    public void testStarInFirstAndMiddle() {

        StarPathElement star = new StarDoublePathElement( "*a*" );

        AssertJUnit.assertTrue( star.stringMatch( "bbbaaccccc" )  );
        AssertJUnit.assertFalse( star.stringMatch( "abbbbbbbbcc" )  );
        AssertJUnit.assertFalse( star.stringMatch( "bbba" )  );

        LiteralPathElement lpe = star.match( "bbbaccc", null );
        // * -> bbb
        // a -> a
        // * -> ccc
        AssertJUnit.assertEquals( "bbbaccc", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "bbb", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "ccc", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );

    }

    @Test
    public void testStarAtFrontAndEnd() {

        StarPathElement star = new StarDoublePathElement( "*a*c" );

        AssertJUnit.assertTrue( star.stringMatch( "bbbbadddc" )  );
        AssertJUnit.assertTrue( star.stringMatch( "bacc" )  );
        AssertJUnit.assertFalse( star.stringMatch( "bac" )  );
        AssertJUnit.assertFalse( star.stringMatch( "baa" )  );

        LiteralPathElement lpe = star.match( "abcadefc", null );
        // * -> abc
        // a -> a index 4
        // * -> def
        // c -> c
        AssertJUnit.assertEquals( "abcadefc", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "abc", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "def", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );

    }

    @Test
    public void testStarAtMiddleAndEnd() {

        StarPathElement star = new StarDoublePathElement( "a*b*" );

        AssertJUnit.assertTrue( star.stringMatch( "adbc" )  );
        AssertJUnit.assertTrue( star.stringMatch( "abbc" )  );
        AssertJUnit.assertFalse( star.stringMatch( "adddddd" )  );
        AssertJUnit.assertFalse( star.stringMatch( "addb" )  );
        AssertJUnit.assertFalse( star.stringMatch( "abc" )  );

        LiteralPathElement lpe = star.match( "abcbbac", null );
        // a -> a
        // * -> bc index 1
        // b -> b   index 3
        // * -> bac index 4
        // c -> c
        AssertJUnit.assertEquals( "abcbbac", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "bc", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "bac", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );

    }


    @Test
    public void testStarsInMiddle() {

        StarPathElement star = new StarDoublePathElement( "a*b*c" );

        AssertJUnit.assertTrue( star.stringMatch( "a123b456c" )  );
        AssertJUnit.assertTrue( star.stringMatch( "abccbcc" )  );

        LiteralPathElement lpe = star.match( "abccbcc", null );
        // a -> a
        // * -> bcc index 1
        // b -> b
        // * -> c index 2
        // c -> c
        AssertJUnit.assertEquals( "abccbcc", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "bcc", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "c", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );

    }


    @Test
    public void testStarsInMiddleNonGreedy() {

        StarPathElement star = new StarDoublePathElement( "a*b*c" );

        LiteralPathElement lpe = star.match( "abbccbccc", null );
        // a -> a
        // * -> b index 1
        // b -> b
        // * -> ccbcc index 2
        // c -> c
        AssertJUnit.assertEquals( "abbccbccc", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "b", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "ccbcc", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );

    }
}
