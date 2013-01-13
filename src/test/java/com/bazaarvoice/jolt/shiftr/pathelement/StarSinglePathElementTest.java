package com.bazaarvoice.jolt.shiftr.pathelement;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class StarSinglePathElementTest {

    @Test
    public void testStarAtFront() {

        StarPathElement star = new StarSinglePathElement( "*-tuna" );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-tuna" )  );
        AssertJUnit.assertTrue( star.stringMatch( "bob-tuna" )  );
        AssertJUnit.assertFalse( star.stringMatch( "-tuna" ) );   // * has to catch something
        AssertJUnit.assertFalse( star.stringMatch( "tuna" ) );
        AssertJUnit.assertFalse( star.stringMatch( "tuna-bob" ) );

        LiteralPathElement lpe = star.match( "bob-tuna", null );
        AssertJUnit.assertEquals( "bob-tuna", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "bob", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( 2, lpe.getSubKeyCount() );

        AssertJUnit.assertNull( star.match( "-tuna", null ) );
    }

    @Test
    public void testStarAtEnd() {

        StarPathElement star = new StarSinglePathElement( "tuna-*" );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-tuna" )  );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-bob" )  );
        AssertJUnit.assertFalse( star.stringMatch( "tuna-" ) );
        AssertJUnit.assertFalse( star.stringMatch( "tuna" ) );
        AssertJUnit.assertFalse( star.stringMatch( "bob-tuna" ) );

        LiteralPathElement lpe = star.match( "tuna-bob", null );
        AssertJUnit.assertEquals( "tuna-bob", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "bob", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( 2, lpe.getSubKeyCount() );

        AssertJUnit.assertNull( star.match( "tuna-", null ) );
    }

    @Test
    public void testStarInMiddle() {

        StarPathElement star = new StarSinglePathElement( "tuna-*-marlin" );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-tuna-marlin" )  );
        AssertJUnit.assertTrue( star.stringMatch( "tuna-bob-marlin" )  );
        AssertJUnit.assertFalse( star.stringMatch( "tuna--marlin" ) );
        AssertJUnit.assertFalse( star.stringMatch( "tunamarlin" ) );
        AssertJUnit.assertFalse( star.stringMatch( "marlin-bob-tuna" ) );

        LiteralPathElement lpe = star.match( "tuna-bob-marlin", null );
        AssertJUnit.assertEquals( "tuna-bob-marlin", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( "bob", lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( 2, lpe.getSubKeyCount() );

        AssertJUnit.assertNull( star.match( "bob", null ) );
    }
}
