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

public class DoubleStarPathElementTest {

    @Test
    public void testStarInFirstAndMiddle() {
        StarPathElement star = new DoubleStarPathElement( "*-tuna-*" );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-tuna-tuna" )  );
        AssertJUnit.assertFalse( star.stringMatch( "-tuna-tuna" )  );
        AssertJUnit.assertFalse( star.stringMatch( "-tuna" )  );

        LiteralPathElement lpe = star.match( "abc-tuna-def", null );
        AssertJUnit.assertEquals( "abc-tuna-def", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "abc", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "def", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );


    }

    @Test
    public void testStarAtFrontAndEnd() {

        StarPathElement star = new DoubleStarPathElement( "*-tuna-*-marlin" );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-tuna-tuna-marlin" )  );
        AssertJUnit.assertFalse( star.stringMatch( "tuna-tuna-tuna-" )  );
        AssertJUnit.assertFalse( star.stringMatch( "tuna-tuna-marlin" )  );

        LiteralPathElement lpe = star.match( "abc-tuna-def-marlin", null );
        AssertJUnit.assertEquals( "abc-tuna-def-marlin", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "abc", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "def", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );



    }

    @Test
    public void testStarAtMiddleAndEnd() {

        StarPathElement star = new DoubleStarPathElement( "tuna-*-anut-*" );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-test-anut-test" )  );
        AssertJUnit.assertFalse( star.stringMatch( "tuna-anut-test" )  );
        AssertJUnit.assertFalse( star.stringMatch( "tuna-anut-test-anut" )  );

        LiteralPathElement lpe = star.match( "tuna-def-anut-test", null );
        AssertJUnit.assertEquals( "tuna-def-anut-test", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "def", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "test", lpe.getSubKeyRef( 2 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );
    }


}
