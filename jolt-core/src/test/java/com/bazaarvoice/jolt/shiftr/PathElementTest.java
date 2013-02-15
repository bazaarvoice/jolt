package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.shiftr.pathelement.AmpPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.ArrayPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.EvaluatablePathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.shiftr.pathelement.PathElement;
import com.bazaarvoice.jolt.shiftr.reference.AmpReference;
import com.bazaarvoice.jolt.shiftr.spec.Spec;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

// Todo Now that the PathElement classes have been split out (no longer inner classes)
//  each class should get a test
public class PathElementTest {

    @Test
    public void referenceTest() {

        ShiftrWriter path = new ShiftrWriter( "SecondaryRatings.tuna-&(0,1)-marlin.Value" );

        AssertJUnit.assertEquals( "SecondaryRatings", path.get( 0 ).getRawKey() );
        AssertJUnit.assertEquals( "SecondaryRatings", path.get( 0 ).toString() );
        AssertJUnit.assertEquals( "Value", path.get( 2 ).getRawKey() );
        AssertJUnit.assertEquals( "Value", path.get( 2 ).toString() );
        AssertJUnit.assertEquals( "Value", path.get( 2 ).toString() );

        AmpPathElement refElement = (AmpPathElement) path.get( 1 );

        AssertJUnit.assertEquals( 3, refElement.getTokens().size() );
        AssertJUnit.assertEquals( "tuna-", (String) refElement.getTokens().get(0) );
        AssertJUnit.assertEquals( "-marlin", (String) refElement.getTokens().get(2) );

        AssertJUnit.assertTrue( refElement.getTokens().get(1) instanceof AmpReference );
        AmpReference ref = (AmpReference) refElement.getTokens().get(1);
        AssertJUnit.assertEquals( 0, ref.getPathIndex() );
        AssertJUnit.assertEquals( 1, ref.getKeyGroup() );
    }

    @Test
    public void arrayRefTest() {

        ShiftrWriter path = new ShiftrWriter( "ugc.photos-&1-bob[&2]" );

        AssertJUnit.assertEquals( 3, path.size() );
        {  // 0
            PathElement pe = path.get( 0 );
            AssertJUnit.assertTrue( "First pathElement should be a literal one.", pe instanceof LiteralPathElement );
        }

        { // 1
            PathElement pe = path.get( 1 );
            AssertJUnit.assertTrue( "Second pathElement should be a literal one.", pe instanceof AmpPathElement );

            AmpPathElement refElement = (AmpPathElement) pe;

            AssertJUnit.assertEquals( 3, refElement.getTokens().size() );

            {
                AssertJUnit.assertTrue( refElement.getTokens().get(0) instanceof String );
                AssertJUnit.assertEquals( "photos-", (String) refElement.getTokens().get(0) );
            }
            {
                AssertJUnit.assertTrue( refElement.getTokens().get(1) instanceof AmpReference );
                AmpReference ref = (AmpReference) refElement.getTokens().get(1);
                AssertJUnit.assertEquals( "&(1,0)", ref.getCanonicalForm() );
                AssertJUnit.assertEquals( 1, ref.getPathIndex() );
                AssertJUnit.assertEquals( 0, ref.getKeyGroup() );
            }
            {
                AssertJUnit.assertTrue( refElement.getTokens().get(2) instanceof String );
                AssertJUnit.assertEquals( "-bob", (String) refElement.getTokens().get(2) );
            }
        }

        { // 2
            PathElement pe = path.get( 2 );
            AssertJUnit.assertTrue( "Third pathElement should be a literal one.", pe instanceof ArrayPathElement );

            ArrayPathElement arrayElement = (ArrayPathElement) pe;
            AssertJUnit.assertEquals( "[&(2,0)]", arrayElement.getCanonicalForm() );
        }
    }

    @Test
    public void calculateOutputTest_refsOnly() {

        MatchablePathElement pe1 = (MatchablePathElement) Spec.parse( "tuna-*-marlin-*" ).get( 0 );
        MatchablePathElement pe2 = (MatchablePathElement) Spec.parse( "rating-*" ).get( 0 );

        LiteralPathElement lpe = pe1.match( "tuna-marlin", new WalkedPath() );
        AssertJUnit.assertNull( lpe );

        lpe = pe1.match( "tuna-A-marlin-AAA", new WalkedPath() );
        AssertJUnit.assertEquals(  "tuna-A-marlin-AAA", lpe.getRawKey() );
        AssertJUnit.assertEquals(  "tuna-A-marlin-AAA", lpe.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( 3, lpe.getSubKeyCount() );
        AssertJUnit.assertEquals( "A" , lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "AAA" , lpe.getSubKeyRef( 2 ) );

        LiteralPathElement lpe2 = pe2.match( "rating-BBB", new WalkedPath( Arrays.asList( lpe ) ) );
        AssertJUnit.assertEquals(  "rating-BBB", lpe2.getRawKey() );
        AssertJUnit.assertEquals(  "rating-BBB", lpe2.getSubKeyRef( 0 ) );
        AssertJUnit.assertEquals( 2, lpe2.getSubKeyCount() );
        AssertJUnit.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        ShiftrWriter outputPath = new ShiftrWriter( "&(1,2).&.value" );
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get( 0 );
            String evaledLeafOutput = outputElement.evaluate( new WalkedPath( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "AAA", evaledLeafOutput );
        }
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get( 1 );
            String evaledLeafOutput = outputElement.evaluate( new WalkedPath( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "rating-BBB", evaledLeafOutput );
        }
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get( 2 );
            String evaledLeafOutput = outputElement.evaluate( new WalkedPath( Arrays.asList( lpe, lpe2 ) ) );
            AssertJUnit.assertEquals( "value", evaledLeafOutput );
        }
    }

    @Test
    public void calculateOutputTest_arrayIndexes() {

        // simulate Shiftr LHS specs
        MatchablePathElement pe1 = (MatchablePathElement) Spec.parse( "tuna-*-marlin-*" ).get( 0 );
        MatchablePathElement pe2 = (MatchablePathElement) Spec.parse( "rating-*" ).get( 0 );

        // match them against some data to get LiteralPathElements with captured values
        LiteralPathElement lpe = pe1.match( "tuna-2-marlin-3", new WalkedPath() );
        AssertJUnit.assertEquals( "2" , lpe.getSubKeyRef( 1 ) );
        AssertJUnit.assertEquals( "3" , lpe.getSubKeyRef( 2 ) );

        LiteralPathElement lpe2 = pe2.match( "rating-BBB", new WalkedPath( Arrays.asList( lpe ) ) );
        AssertJUnit.assertEquals( 2, lpe2.getSubKeyCount() );
        AssertJUnit.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        // Build an write path path
        ShiftrWriter shiftrWriter = new ShiftrWriter( "tuna[&(1,1)].marlin[&(1,2)].&(0,1)" );

        AssertJUnit.assertEquals( 5, shiftrWriter.size() );
        AssertJUnit.assertEquals( "tuna.[&(1,1)].marlin.[&(1,2)].&(0,1)", shiftrWriter.getCanonicalForm() );

        // Evaluate the write path against the LiteralPath elements we build above ( like Shiftr does )
        WalkedPath walkedPath = new WalkedPath( Arrays.asList( lpe, lpe2 ) );
        List<String> stringPath = shiftrWriter.evaluate( walkedPath );

        AssertJUnit.assertEquals( "tuna",   stringPath.get( 0 ) );
        AssertJUnit.assertEquals( "2",      stringPath.get( 1 ) );
        AssertJUnit.assertEquals( "marlin", stringPath.get( 2 ) );
        AssertJUnit.assertEquals( "3",      stringPath.get( 3 ) );
        AssertJUnit.assertEquals( "BBB",    stringPath.get( 4 ) );
    }
}
