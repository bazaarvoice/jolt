package com.bazaarvoice.jolt.shiftr;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

public class PathElementTest {

    @Test
    public void referenceTest() {

        Path<PathElement> path = Path.parseDotNotation( "SecondaryRatings.tuna-&0(1)-marlin.Value" );

        AssertJUnit.assertEquals( "SecondaryRatings", path.elementAt(0).rawKey );
        AssertJUnit.assertEquals( "SecondaryRatings", path.elementAt(0).toString() );
        AssertJUnit.assertEquals( "Value", path.elementFromEnd(0).rawKey );
        AssertJUnit.assertEquals( "Value", path.elementFromEnd( 0 ).toString() );
        AssertJUnit.assertEquals( "Value", path.lastElement().toString() );

        PathElement.ReferencePathElement refElement = (PathElement.ReferencePathElement) path.elementAt(1);

        AssertJUnit.assertEquals( 3, refElement.tokens.size() );
        AssertJUnit.assertEquals( "tuna-", (String) refElement.tokens.get(0) );
        AssertJUnit.assertEquals( "-marlin", (String) refElement.tokens.get(2) );

        AssertJUnit.assertTrue( refElement.tokens.get(1) instanceof Reference );
        Reference ref = (Reference) refElement.tokens.get(1);
        AssertJUnit.assertFalse( ref.isArray );
        AssertJUnit.assertEquals( 0, ref.pathIndex );
        AssertJUnit.assertEquals( 1, ref.keyGroup );
    }

    @Test
    public void arrayRefTest() {

        Path<PathElement> path = Path.parseDotNotation( "ugc.photos-&1-[&(1)]" );

        PathElement.ReferencePathElement refElement = (PathElement.ReferencePathElement) path.elementAt(1);

        AssertJUnit.assertEquals( 4, refElement.tokens.size() );

        {
            AssertJUnit.assertTrue( refElement.tokens.get(0) instanceof String );
            AssertJUnit.assertEquals( "photos-", (String) refElement.tokens.get(0) );
        }
        {
            AssertJUnit.assertTrue( refElement.tokens.get(1) instanceof Reference );
            Reference ref = (Reference) refElement.tokens.get(1);
            AssertJUnit.assertFalse( ref.isArray );
            AssertJUnit.assertEquals( 1, ref.pathIndex );
            AssertJUnit.assertEquals( 0, ref.keyGroup );
        }
        {
            AssertJUnit.assertTrue( refElement.tokens.get(2) instanceof String );
            AssertJUnit.assertEquals( "-", (String) refElement.tokens.get(2) );
        }
        {
            AssertJUnit.assertTrue( refElement.tokens.get(3) instanceof Reference );
            Reference ref = (Reference) refElement.tokens.get(3);
            AssertJUnit.assertTrue( ref.isArray );
            AssertJUnit.assertEquals( 0, ref.pathIndex );
            AssertJUnit.assertEquals( 1, ref.keyGroup );
        }
    }

    @Test
    public void calculateOutputTest() {

        // this should make AAA.rating-BBB.value : CCC

        PathElement pe1 = PathElement.parse( "tuna-*-marlin-*" );
        PathElement pe2 = PathElement.parse(    "rating-*" );

        PathElement leafOutput = PathElement.parse( "&1(2).&.value" );

        PathElement.LiteralPathElement lpe = pe1.matchInput( "tuna-marlin", new Path<PathElement.LiteralPathElement>( Collections.<PathElement.LiteralPathElement>emptyList() ) );
        AssertJUnit.assertNull( lpe );

        lpe = pe1.matchInput( "tuna-A-marlin-AAA", new Path<PathElement.LiteralPathElement>( Collections.<PathElement.LiteralPathElement>emptyList() ) );
        AssertJUnit.assertEquals(  "tuna-A-marlin-AAA", lpe.rawKey );
        AssertJUnit.assertEquals(  "tuna-A-marlin-AAA", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );
        AssertJUnit.assertEquals( "A" , lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "AAA" , lpe.getSubKeyRef( 2 ) );

        PathElement.LiteralPathElement lpe2 = pe2.matchInput( "rating-BBB", new Path<PathElement.LiteralPathElement>( Arrays.asList( lpe ) ) );
        AssertJUnit.assertEquals(  "rating-BBB", lpe2.rawKey );
        AssertJUnit.assertEquals(  "rating-BBB", lpe2.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( 2, lpe2.getSubKeyCount() );
        AssertJUnit.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        String evaledLeafOutput = leafOutput.evaluateAsOutputKey( new Path<PathElement.LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );

        AssertJUnit.assertEquals( "AAA.rating-BBB.value", evaledLeafOutput );
    }
}
