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
import org.testng.annotations.Test;

public class StarSinglePathElementTest {

    @Test
    public void testStarAtFront() {

        StarPathElement star = new StarSinglePathElement( "*-tuna" );
        Assert.assertTrue( star.stringMatch( "tuna-tuna" )  );
        Assert.assertTrue( star.stringMatch( "bob-tuna" )  );
        Assert.assertFalse( star.stringMatch( "-tuna" ) );   // * has to catch something
        Assert.assertFalse( star.stringMatch( "tuna" ) );
        Assert.assertFalse( star.stringMatch( "tuna-bob" ) );

        MatchedElement lpe = star.match( "bob-tuna", null );
        Assert.assertEquals( "bob-tuna", lpe.getSubKeyRef( 0 ) );
        Assert.assertEquals( "bob", lpe.getSubKeyRef( 1 ) );
        Assert.assertEquals( 2, lpe.getSubKeyCount() );

        Assert.assertNull( star.match( "-tuna", null ) );
    }

    @Test
    public void testStarAtEnd() {

        StarPathElement star = new StarSinglePathElement( "tuna-*" );
        Assert.assertTrue( star.stringMatch( "tuna-tuna" )  );
        Assert.assertTrue( star.stringMatch( "tuna-bob" )  );
        Assert.assertFalse( star.stringMatch( "tuna-" ) );
        Assert.assertFalse( star.stringMatch( "tuna" ) );
        Assert.assertFalse( star.stringMatch( "bob-tuna" ) );

        MatchedElement lpe = star.match( "tuna-bob", null );
        Assert.assertEquals( "tuna-bob", lpe.getSubKeyRef( 0 ) );
        Assert.assertEquals( "bob", lpe.getSubKeyRef( 1 ) );
        Assert.assertEquals( 2, lpe.getSubKeyCount() );

        Assert.assertNull( star.match( "tuna-", null ) );
    }

    @Test
    public void testStarInMiddle() {

        StarPathElement star = new StarSinglePathElement( "tuna-*-marlin" );
        Assert.assertTrue( star.stringMatch( "tuna-tuna-marlin" )  );
        Assert.assertTrue( star.stringMatch( "tuna-bob-marlin" )  );
        Assert.assertFalse( star.stringMatch( "tuna--marlin" ) );
        Assert.assertFalse( star.stringMatch( "tunamarlin" ) );
        Assert.assertFalse( star.stringMatch( "marlin-bob-tuna" ) );

        MatchedElement lpe = star.match( "tuna-bob-marlin", null );
        Assert.assertEquals( "tuna-bob-marlin", lpe.getSubKeyRef( 0 ) );
        Assert.assertEquals( "bob", lpe.getSubKeyRef( 1 ) );
        Assert.assertEquals( 2, lpe.getSubKeyCount() );

        Assert.assertNull( star.match( "bob", null ) );
    }
}
