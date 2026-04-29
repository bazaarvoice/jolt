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
package io.joltcommunity.jolt.common.pathelement;

import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.exception.SpecException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class StarSinglePathElementTest {

    @Test
    public void testStarAtFront() {

        StarPathElement star = new StarSinglePathElement("*-tuna");
        Assert.assertTrue(star.stringMatch("tuna-tuna"));
        Assert.assertTrue(star.stringMatch("bob-tuna"));
        Assert.assertFalse(star.stringMatch("-tuna"));   // * has to catch something
        Assert.assertFalse(star.stringMatch("tuna"));
        Assert.assertFalse(star.stringMatch("tuna-bob"));

        MatchedElement lpe = star.match("bob-tuna", null);
        Assert.assertEquals(lpe.getSubKeyRef(0), "bob-tuna");
        Assert.assertEquals(lpe.getSubKeyRef(1), "bob");
        Assert.assertEquals(lpe.getSubKeyCount(), 2);

        Assert.assertNull(star.match("-tuna", null));
    }

    @Test
    public void testStarAtEnd() {

        StarPathElement star = new StarSinglePathElement("tuna-*");
        Assert.assertTrue(star.stringMatch("tuna-tuna"));
        Assert.assertTrue(star.stringMatch("tuna-bob"));
        Assert.assertFalse(star.stringMatch("tuna-"));
        Assert.assertFalse(star.stringMatch("tuna"));
        Assert.assertFalse(star.stringMatch("bob-tuna"));

        MatchedElement lpe = star.match("tuna-bob", null);
        Assert.assertEquals(lpe.getSubKeyRef(0), "tuna-bob");
        Assert.assertEquals(lpe.getSubKeyRef(1), "bob");
        Assert.assertEquals(lpe.getSubKeyCount(), 2);

        Assert.assertNull(star.match("tuna-", null));
    }

    @Test
    public void testStarInMiddle() {

        StarPathElement star = new StarSinglePathElement("tuna-*-marlin");
        Assert.assertTrue(star.stringMatch("tuna-tuna-marlin"));
        Assert.assertTrue(star.stringMatch("tuna-bob-marlin"));
        Assert.assertFalse(star.stringMatch("tuna--marlin"));
        Assert.assertFalse(star.stringMatch("tunamarlin"));
        Assert.assertFalse(star.stringMatch("marlin-bob-tuna"));

        MatchedElement lpe = star.match("tuna-bob-marlin", null);
        Assert.assertEquals(lpe.getSubKeyRef(0), "tuna-bob-marlin");
        Assert.assertEquals(lpe.getSubKeyRef(1), "bob");
        Assert.assertEquals(lpe.getSubKeyCount(), 2);

        Assert.assertNull(star.match("bob", null));
    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarSinglePathElement should only have one '\\*' in its key\\. Was: .*")
    public void testMultipleStarsThrowsException() {
        new StarSinglePathElement("foo**bar");
    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarSinglePathElement should have a key that is just '\\*'\\. Was: \\*")
    public void testSingleStarThrowsException() {
        new StarSinglePathElement("*");
    }
}
