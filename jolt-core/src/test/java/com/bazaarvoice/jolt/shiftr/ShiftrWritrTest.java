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
package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.PathElementBuilder;
import com.bazaarvoice.jolt.common.pathelement.AmpPathElement;
import com.bazaarvoice.jolt.common.pathelement.ArrayPathElement;
import com.bazaarvoice.jolt.common.pathelement.EvaluatablePathElement;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.reference.AmpReference;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

// Todo Now that the PathElement classes have been split out (no longer inner classes)
//  each class should get a test
public class ShiftrWritrTest {

    @Test
    public void referenceTest() {

        ShiftrWriter path = new ShiftrWriter( "SecondaryRatings.tuna-&(0,1)-marlin.Value" );

        Assert.assertEquals( "SecondaryRatings", path.get( 0 ).getRawKey() );
        Assert.assertEquals( "SecondaryRatings", path.get( 0 ).toString() );
        Assert.assertEquals( "Value", path.get( 2 ).getRawKey() );
        Assert.assertEquals( "Value", path.get( 2 ).toString() );
        Assert.assertEquals( "Value", path.get( 2 ).toString() );

        AmpPathElement refElement = (AmpPathElement) path.get( 1 );

        Assert.assertEquals( 3, refElement.getTokens().size() );
        Assert.assertEquals( "tuna-", (String) refElement.getTokens().get(0) );
        Assert.assertEquals( "-marlin", (String) refElement.getTokens().get(2) );

        Assert.assertTrue( refElement.getTokens().get(1) instanceof AmpReference );
        AmpReference ref = (AmpReference) refElement.getTokens().get(1);
        Assert.assertEquals( 0, ref.getPathIndex() );
        Assert.assertEquals( 1, ref.getKeyGroup() );
    }

    @Test
    public void arrayRefTest() {

        ShiftrWriter path = new ShiftrWriter( "ugc.photos-&1-bob[&2]" );

        Assert.assertEquals( 3, path.size() );
        {  // 0
            PathElement pe = path.get( 0 );
            Assert.assertTrue( pe instanceof LiteralPathElement, "First pathElement should be a literal one." );
        }

        { // 1
            PathElement pe = path.get( 1 );
            Assert.assertTrue( pe instanceof AmpPathElement, "Second pathElement should be a AmpPathElement." );

            AmpPathElement refElement = (AmpPathElement) pe;

            Assert.assertEquals( 3, refElement.getTokens().size() );

            {
                Assert.assertTrue( refElement.getTokens().get(0) instanceof String );
                Assert.assertEquals( "photos-", (String) refElement.getTokens().get(0) );
            }
            {
                Assert.assertTrue( refElement.getTokens().get(1) instanceof AmpReference );
                AmpReference ref = (AmpReference) refElement.getTokens().get(1);
                Assert.assertEquals( "&(1,0)", ref.getCanonicalForm() );
                Assert.assertEquals( 1, ref.getPathIndex() );
                Assert.assertEquals( 0, ref.getKeyGroup() );
            }
            {
                Assert.assertTrue( refElement.getTokens().get(2) instanceof String );
                Assert.assertEquals( "-bob", (String) refElement.getTokens().get(2) );
            }
        }

        { // 2
            PathElement pe = path.get( 2 );
            Assert.assertTrue( pe instanceof ArrayPathElement, "Third pathElement should be a literal one." );

            ArrayPathElement arrayElement = (ArrayPathElement) pe;
            Assert.assertEquals( "[&(2,0)]", arrayElement.getCanonicalForm() );
        }
    }

    @Test
    public void calculateOutputTest_refsOnly() {

        MatchablePathElement pe1 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS( "tuna-*-marlin-*" );
        MatchablePathElement pe2 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS( "rating-*" );

        MatchedElement lpe = pe1.match( "tuna-marlin", new WalkedPath() );
        Assert.assertNull( lpe );

        lpe = pe1.match( "tuna-A-marlin-AAA", new WalkedPath() );
        Assert.assertEquals(  "tuna-A-marlin-AAA", lpe.getRawKey() );
        Assert.assertEquals(  "tuna-A-marlin-AAA", lpe.getSubKeyRef( 0 ) );
        Assert.assertEquals( 3, lpe.getSubKeyCount() );
        Assert.assertEquals( "A" , lpe.getSubKeyRef( 1 ) );
        Assert.assertEquals( "AAA" , lpe.getSubKeyRef( 2 ) );

        MatchedElement lpe2 = pe2.match( "rating-BBB", new WalkedPath( null, lpe ) );
        Assert.assertEquals(  "rating-BBB", lpe2.getRawKey() );
        Assert.assertEquals(  "rating-BBB", lpe2.getSubKeyRef( 0 ) );
        Assert.assertEquals( 2, lpe2.getSubKeyCount() );
        Assert.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        ShiftrWriter outputPath = new ShiftrWriter( "&(1,2).&.value" );
        WalkedPath twoSteps = new WalkedPath( null, lpe );
        twoSteps.add( null, lpe2 );
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get( 0 );
            String evaledLeafOutput = outputElement.evaluate( twoSteps );
            Assert.assertEquals( "AAA", evaledLeafOutput );
        }
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get( 1 );
            String evaledLeafOutput = outputElement.evaluate( twoSteps );
            Assert.assertEquals( "rating-BBB", evaledLeafOutput );
        }
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get( 2 );
            String evaledLeafOutput = outputElement.evaluate( twoSteps );
            Assert.assertEquals( "value", evaledLeafOutput );
        }
    }

    @Test
    public void calculateOutputTest_arrayIndexes() {

        // simulate Shiftr LHS specs
        MatchablePathElement pe1 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS( "tuna-*-marlin-*" );
        MatchablePathElement pe2 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS( "rating-*" );

        // match them against some data to get LiteralPathElements with captured values
        MatchedElement lpe = pe1.match( "tuna-2-marlin-3", new WalkedPath() );
        Assert.assertEquals( "2" , lpe.getSubKeyRef( 1 ) );
        Assert.assertEquals( "3" , lpe.getSubKeyRef( 2 ) );

        MatchedElement lpe2 = pe2.match( "rating-BBB", new WalkedPath( null, lpe ) );
        Assert.assertEquals( 2, lpe2.getSubKeyCount() );
        Assert.assertEquals( "BBB" , lpe2.getSubKeyRef( 1 ) );

        // Build an write path path
        ShiftrWriter shiftrWriter = new ShiftrWriter( "tuna[&(1,1)].marlin[&(1,2)].&(0,1)" );

        Assert.assertEquals( 5, shiftrWriter.size() );
        Assert.assertEquals( "tuna.[&(1,1)].marlin.[&(1,2)].&(0,1)", shiftrWriter.getCanonicalForm() );

        // Evaluate the write path against the LiteralPath elements we build above ( like Shiftr does )
        WalkedPath twoSteps = new WalkedPath( null, lpe );
        twoSteps.add( null, lpe2 );
        List<String> stringPath = shiftrWriter.evaluate( twoSteps );

        Assert.assertEquals( "tuna",   stringPath.get( 0 ) );
        Assert.assertEquals( "2",      stringPath.get( 1 ) );
        Assert.assertEquals( "marlin", stringPath.get( 2 ) );
        Assert.assertEquals( "3",      stringPath.get( 3 ) );
        Assert.assertEquals( "BBB",    stringPath.get( 4 ) );
    }
}
