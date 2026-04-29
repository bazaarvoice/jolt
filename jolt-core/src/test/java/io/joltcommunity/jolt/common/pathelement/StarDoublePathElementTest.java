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

public class StarDoublePathElementTest {

    @Test
    public void testStarInFirstAndMiddle() {

        StarPathElement star = new StarDoublePathElement("*a*");

        Assert.assertTrue(star.stringMatch("bbbaaccccc"));
        Assert.assertFalse(star.stringMatch("abbbbbbbbcc"));
        Assert.assertFalse(star.stringMatch("bbba"));

        MatchedElement lpe = star.match("bbbaccc", null);
        // * -> bbb
        // a -> a
        // * -> ccc
        Assert.assertEquals(lpe.getSubKeyRef(0), "bbbaccc");
        Assert.assertEquals(lpe.getSubKeyRef(1), "bbb");
        Assert.assertEquals(lpe.getSubKeyRef(2), "ccc");
        Assert.assertEquals(lpe.getSubKeyCount(), 3);

    }

    @Test
    public void testStarAtFrontAndEnd() {

        StarPathElement star = new StarDoublePathElement("*a*c");

        Assert.assertTrue(star.stringMatch("bbbbadddc"));
        Assert.assertTrue(star.stringMatch("bacc"));
        Assert.assertFalse(star.stringMatch("bac"));
        Assert.assertFalse(star.stringMatch("baa"));

        MatchedElement lpe = star.match("abcadefc", null);
        // * -> abc
        // a -> a index 4
        // * -> def
        // c -> c
        Assert.assertEquals(lpe.getSubKeyRef(0), "abcadefc");
        Assert.assertEquals(lpe.getSubKeyRef(1), "abc");
        Assert.assertEquals(lpe.getSubKeyRef(2), "def");
        Assert.assertEquals(lpe.getSubKeyCount(), 3);

    }

    @Test
    public void testStarAtMiddleAndEnd() {

        StarPathElement star = new StarDoublePathElement("a*b*");

        Assert.assertTrue(star.stringMatch("adbc"));
        Assert.assertTrue(star.stringMatch("abbc"));
        Assert.assertFalse(star.stringMatch("adddddd"));
        Assert.assertFalse(star.stringMatch("addb"));
        Assert.assertFalse(star.stringMatch("abc"));

        MatchedElement lpe = star.match("abcbbac", null);
        // a -> a
        // * -> bc index 1
        // b -> b   index 3
        // * -> bac index 4
        // c -> c
        Assert.assertEquals(lpe.getSubKeyRef(0), "abcbbac");
        Assert.assertEquals(lpe.getSubKeyRef(1), "bc");
        Assert.assertEquals(lpe.getSubKeyRef(2), "bac");
        Assert.assertEquals(lpe.getSubKeyCount(), 3);

    }


    @Test
    public void testStarsInMiddle() {

        StarPathElement star = new StarDoublePathElement("a*b*c");

        Assert.assertTrue(star.stringMatch("a123b456c"));
        Assert.assertTrue(star.stringMatch("abccbcc"));

        MatchedElement lpe = star.match("abccbcc", null);
        // a -> a
        // * -> bcc index 1
        // b -> b
        // * -> c index 2
        // c -> c
        Assert.assertEquals(lpe.getSubKeyRef(0), "abccbcc");
        Assert.assertEquals(lpe.getSubKeyRef(1), "bcc");
        Assert.assertEquals(lpe.getSubKeyRef(2), "c");
        Assert.assertEquals(lpe.getSubKeyCount(), 3);

    }


    @Test
    public void testStarsInMiddleNonGreedy() {

        StarPathElement star = new StarDoublePathElement("a*b*c");

        MatchedElement lpe = star.match("abbccbccc", null);
        // a -> a
        // * -> b index 1
        // b -> b
        // * -> ccbcc index 2
        // c -> c
        Assert.assertEquals(lpe.getSubKeyRef(0), "abbccbccc");
        Assert.assertEquals(lpe.getSubKeyRef(1), "b");
        Assert.assertEquals(lpe.getSubKeyRef(2), "ccbcc");
        Assert.assertEquals(lpe.getSubKeyCount(), 3);

    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarDoublePathElement should have two '\\*' in its key\\. Was: abc")
    public void testNoStarThrowsException() {
        new StarDoublePathElement("abc");
    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarDoublePathElement should have two '\\*' in its key\\. Was: a\\*b")
    public void testOneStarThrowsException() {
        new StarDoublePathElement("a*b");
    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarDoublePathElement should have two '\\*' in its key\\. Was: a\\*b\\*c\\*d")
    public void testMoreThanTwoStarsThrowsException() {
        new StarDoublePathElement("a*b*c*d");
    }
}
