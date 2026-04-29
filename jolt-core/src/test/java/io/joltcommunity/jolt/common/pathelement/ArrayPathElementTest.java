/*
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

import io.joltcommunity.jolt.common.reference.AmpReference;
import io.joltcommunity.jolt.common.reference.HashReference;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ArrayPathElementTest {
    @Test
    public void testConstructorAutoExpand() {
        ArrayPathElement ape = new ArrayPathElement("[]");
        Assert.assertEquals(ape.getCanonicalForm(), "[]");
        Assert.assertFalse(ape.isExplicitArrayIndex());
    }

    @Test
    public void testConstructorExplicitIndex() {
        ArrayPathElement ape = new ArrayPathElement("[3]");
        Assert.assertEquals(ape.getCanonicalForm(), "[3]");
        Assert.assertTrue(ape.isExplicitArrayIndex());
        Assert.assertEquals(ape.getExplicitArrayIndex(), Integer.valueOf(3));
    }

    @Test
    public void testConstructorReference() {
        ArrayPathElement ape = new ArrayPathElement("[&0]");
        Assert.assertEquals(ape.getCanonicalForm(), "[&(0,0)]");
    }

    @Test
    public void testConstructorHash() {
        ArrayPathElement ape = new ArrayPathElement("[#0]");
        Assert.assertEquals(ape.getCanonicalForm(), "[#0]");
    }

    @Test
    public void testConstructorTranspose() {
        ArrayPathElement ape = new ArrayPathElement("[@(1,foo)]");
        Assert.assertEquals(ape.getCanonicalForm(), "[@(1,foo)]");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testConstructorInvalidKeyNoBrackets() {
        new ArrayPathElement("foo");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testConstructorInvalidExplicitIndex() {
        new ArrayPathElement("[abc]");
    }
}
