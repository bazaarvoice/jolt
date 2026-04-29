/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt.shiftr;

import io.joltcommunity.jolt.common.PathElementBuilder;
import io.joltcommunity.jolt.common.pathelement.*;
import io.joltcommunity.jolt.common.reference.AmpReference;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

// Todo Now that the PathElement classes have been split out (no longer inner classes)
//  each class should get a test
public class ShiftrWritrTest {

    @Test
    public void referenceTest() {

        ShiftrWriter path = new ShiftrWriter("SecondaryRatings.tuna-&(0,1)-marlin.Value");

        Assert.assertEquals(path.get(0).getRawKey(), "SecondaryRatings");
        Assert.assertEquals(path.get(0).toString(), "SecondaryRatings");
        Assert.assertEquals(path.get(2).getRawKey(), "Value");
        Assert.assertEquals(path.get(2).toString(), "Value");
        Assert.assertEquals(path.get(2).toString(), "Value");

        AmpPathElement refElement = (AmpPathElement) path.get(1);

        Assert.assertEquals(refElement.getTokens().size(), 3);
        Assert.assertEquals((String) refElement.getTokens().get(0), "tuna-");
        Assert.assertEquals((String) refElement.getTokens().get(2), "-marlin");

        Assert.assertTrue(refElement.getTokens().get(1) instanceof AmpReference);
        AmpReference ref = (AmpReference) refElement.getTokens().get(1);
        Assert.assertEquals(ref.getPathIndex(), 0);
        Assert.assertEquals(ref.getKeyGroup(), 1);
    }

    @Test
    public void arrayRefTest() {

        ShiftrWriter path = new ShiftrWriter("ugc.photos-&1-bob[&2]");

        Assert.assertEquals(path.size(), 3);
        {  // 0
            PathElement pe = path.get(0);
            Assert.assertTrue(pe instanceof LiteralPathElement, "First pathElement should be a literal one.");
        }

        { // 1
            PathElement pe = path.get(1);
            Assert.assertTrue(pe instanceof AmpPathElement, "Second pathElement should be a AmpPathElement.");

            AmpPathElement refElement = (AmpPathElement) pe;

            Assert.assertEquals(refElement.getTokens().size(), 3);

            {
                Assert.assertTrue(refElement.getTokens().get(0) instanceof String);
                Assert.assertEquals((String) refElement.getTokens().get(0), "photos-");
            }
            {
                Assert.assertTrue(refElement.getTokens().get(1) instanceof AmpReference);
                AmpReference ref = (AmpReference) refElement.getTokens().get(1);
                Assert.assertEquals(ref.getCanonicalForm(), "&(1,0)");
                Assert.assertEquals(ref.getPathIndex(), 1);
                Assert.assertEquals(ref.getKeyGroup(), 0);
            }
            {
                Assert.assertTrue(refElement.getTokens().get(2) instanceof String);
                Assert.assertEquals((String) refElement.getTokens().get(2), "-bob");
            }
        }

        { // 2
            PathElement pe = path.get(2);
            Assert.assertTrue(pe instanceof ArrayPathElement, "Third pathElement should be a literal one.");

            ArrayPathElement arrayElement = (ArrayPathElement) pe;
            Assert.assertEquals(arrayElement.getCanonicalForm(), "[&(2,0)]");
        }
    }

    @Test
    public void calculateOutputTest_refsOnly() {

        MatchablePathElement pe1 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS("tuna-*-marlin-*");
        MatchablePathElement pe2 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS("rating-*");

        MatchedElement lpe = pe1.match("tuna-marlin", new WalkedPath());
        Assert.assertNull(lpe);

        lpe = pe1.match("tuna-A-marlin-AAA", new WalkedPath());
        Assert.assertEquals(lpe.getRawKey(), "tuna-A-marlin-AAA");
        Assert.assertEquals(lpe.getSubKeyRef(0), "tuna-A-marlin-AAA");
        Assert.assertEquals(lpe.getSubKeyCount(), 3);
        Assert.assertEquals(lpe.getSubKeyRef(1), "A");
        Assert.assertEquals(lpe.getSubKeyRef(2), "AAA");

        MatchedElement lpe2 = pe2.match("rating-BBB", new WalkedPath(null, lpe));
        Assert.assertEquals(lpe2.getRawKey(), "rating-BBB");
        Assert.assertEquals(lpe2.getSubKeyRef(0), "rating-BBB");
        Assert.assertEquals(lpe2.getSubKeyCount(), 2);
        Assert.assertEquals(lpe2.getSubKeyRef(1), "BBB");

        ShiftrWriter outputPath = new ShiftrWriter("&(1,2).&.value");
        WalkedPath twoSteps = new WalkedPath(null, lpe);
        twoSteps.add(null, lpe2);
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get(0);
            String evaledLeafOutput = outputElement.evaluate(twoSteps);
            Assert.assertEquals(evaledLeafOutput, "AAA");
        }
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get(1);
            String evaledLeafOutput = outputElement.evaluate(twoSteps);
            Assert.assertEquals(evaledLeafOutput, "rating-BBB");
        }
        {
            EvaluatablePathElement outputElement = (EvaluatablePathElement) outputPath.get(2);
            String evaledLeafOutput = outputElement.evaluate(twoSteps);
            Assert.assertEquals(evaledLeafOutput, "value");
        }
    }

    @Test
    public void calculateOutputTest_arrayIndexes() {

        // simulate Shiftr LHS specs
        MatchablePathElement pe1 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS("tuna-*-marlin-*");
        MatchablePathElement pe2 = (MatchablePathElement) PathElementBuilder.parseSingleKeyLHS("rating-*");

        // match them against some data to get LiteralPathElements with captured values
        MatchedElement lpe = pe1.match("tuna-2-marlin-3", new WalkedPath());
        Assert.assertEquals(lpe.getSubKeyRef(1), "2");
        Assert.assertEquals(lpe.getSubKeyRef(2), "3");

        MatchedElement lpe2 = pe2.match("rating-BBB", new WalkedPath(null, lpe));
        Assert.assertEquals(lpe2.getSubKeyCount(), 2);
        Assert.assertEquals(lpe2.getSubKeyRef(1), "BBB");

        // Build an write path path
        ShiftrWriter shiftrWriter = new ShiftrWriter("tuna[&(1,1)].marlin[&(1,2)].&(0,1)");

        Assert.assertEquals(shiftrWriter.size(), 5);
        Assert.assertEquals(shiftrWriter.getCanonicalForm(), "tuna.[&(1,1)].marlin.[&(1,2)].&(0,1)");

        // Evaluate the write path against the LiteralPath elements we build above ( like Shiftr does )
        WalkedPath twoSteps = new WalkedPath(null, lpe);
        twoSteps.add(null, lpe2);
        List<String> stringPath = shiftrWriter.evaluate(twoSteps);

        Assert.assertEquals(stringPath.get(0), "tuna");
        Assert.assertEquals(stringPath.get(1), "2");
        Assert.assertEquals(stringPath.get(2), "marlin");
        Assert.assertEquals(stringPath.get(3), "3");
        Assert.assertEquals(stringPath.get(4), "BBB");
    }
}
