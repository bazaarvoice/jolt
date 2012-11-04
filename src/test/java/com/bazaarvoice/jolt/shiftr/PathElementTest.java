package com.bazaarvoice.jolt.shiftr;
import com.bazaarvoice.jolt.shiftr.Path.*;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.bazaarvoice.jolt.shiftr.PathElement.*;

public class PathElementTest {

    @Test
    public void referenceTest() {

        DotNotationPath path = DotNotationPath.parseDotNotation( "SecondaryRatings.tuna-&(0,1)-marlin.Value" );

        AssertJUnit.assertEquals( "SecondaryRatings", path.elementAt(0).getRawKey() );
        AssertJUnit.assertEquals( "SecondaryRatings", path.elementAt(0).toString() );
        AssertJUnit.assertEquals( "Value", path.elementFromEnd(0).getRawKey() );
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

        DotNotationPath path = DotNotationPath.parseDotNotation( "ugc.photos-&1-[&(0,1)]" );

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
    public void calculateOutputTest_refsOnly() {

        PathElement pe1 = PathElement.parse( "tuna-*-marlin-*" );
        PathElement pe2 = PathElement.parse(    "rating-*" );

        LiteralPathElement lpe = pe1.match( "tuna-marlin", new Path<LiteralPathElement>( Collections.<LiteralPathElement>emptyList() ) );
        AssertJUnit.assertNull( lpe );

        lpe = pe1.match( "tuna-A-marlin-AAA", new Path<LiteralPathElement>( Collections.<LiteralPathElement>emptyList() ) );
        AssertJUnit.assertEquals(  "tuna-A-marlin-AAA", lpe.getRawKey() );
        AssertJUnit.assertEquals(  "tuna-A-marlin-AAA", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );
        AssertJUnit.assertEquals( "A" , lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "AAA" , lpe.getSubKeyRef( 2 ) );

        LiteralPathElement lpe2 = pe2.match( "rating-BBB", new Path<LiteralPathElement>( Arrays.asList( lpe ) ) );
        AssertJUnit.assertEquals(  "rating-BBB", lpe2.getRawKey() );
        AssertJUnit.assertEquals(  "rating-BBB", lpe2.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( 2, lpe2.getSubKeyCount() );
        AssertJUnit.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        DotNotationPath outputPath = DotNotationPath.parseDotNotation( "&(1,2).&.value" );
        {
            PathElement outputElement = outputPath.elementAt( 0 );
            String evaledLeafOutput = outputElement.evaluate( new Path<LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "AAA", evaledLeafOutput );
        }
        {
            PathElement outputElement = outputPath.elementAt( 1 );
            String evaledLeafOutput = outputElement.evaluate( new Path<LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "rating-BBB", evaledLeafOutput );
        }
        {
            PathElement outputElement = outputPath.elementAt( 2 );
            String evaledLeafOutput = outputElement.evaluate( new Path<LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "value", evaledLeafOutput );
        }
    }

    @Test
    public void calculateOutputTest_arrayIndexes() {

        PathElement pe1 = PathElement.parse( "tuna-*-marlin-*" );
        PathElement pe2 = PathElement.parse(    "rating-*" );

        LiteralPathElement lpe = pe1.match( "tuna-2-marlin-3", new Path<LiteralPathElement>( Collections.<LiteralPathElement>emptyList() ) );
        AssertJUnit.assertEquals( "2" , lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "3" , lpe.getSubKeyRef( 2 ) );

        LiteralPathElement lpe2 = pe2.match( "rating-BBB", new Path<LiteralPathElement>( Arrays.asList( lpe ) ) );
        AssertJUnit.assertEquals( 2, lpe2.getSubKeyCount() );
        AssertJUnit.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        DotNotationPath outputPath = DotNotationPath.parseDotNotation( "tuna[&(1,1)].marlin[&(1,2)].&(0,1)" );
        {
            PathElement outputElement = outputPath.elementAt( 0 );
            String evaledLeafOutput = outputElement.evaluate( new Path<LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "tuna[2]", evaledLeafOutput );
        }
        {
            PathElement outputElement = outputPath.elementAt( 1 );
            String evaledLeafOutput = outputElement.evaluate( new Path<LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "marlin[3]", evaledLeafOutput );
        }
        {
            PathElement outputElement = outputPath.elementAt( 2 );
            String evaledLeafOutput = outputElement.evaluate( new Path<LiteralPathElement>( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "BBB", evaledLeafOutput );
        }
    }
}
