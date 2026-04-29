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
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class AmpPathElementTest {

    @Test
    public void testConstructorWithLiteralOnly() {
        AmpPathElement element = new AmpPathElement("photos");
        List<Object> tokens = element.getTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertEquals(tokens.get(0), "photos");
        Assert.assertEquals(element.getCanonicalForm(), "photos");
    }

    @Test
    public void testConstructorWithReferenceOnly() {
        AmpPathElement element = new AmpPathElement("&(1,1)");
        List<Object> tokens = element.getTokens();
        Assert.assertEquals(tokens.size(), 1);
        Assert.assertTrue(tokens.get(0) instanceof AmpReference);
        Assert.assertEquals(element.getCanonicalForm(), "&(1,1)");
    }

    @Test
    public void testConstructorWithLiteralAndReference() {
        AmpPathElement element = new AmpPathElement("photos-&(1,1)");
        List<Object> tokens = element.getTokens();
        Assert.assertEquals(tokens.size(), 2);
        Assert.assertEquals(tokens.get(0), "photos-");
        Assert.assertTrue(tokens.get(1) instanceof AmpReference);
        Assert.assertEquals(element.getCanonicalForm(), "photos-&(1,1)");
    }

    @Test
    public void testConstructorWithMultipleReferences() {
        AmpPathElement element = new AmpPathElement("a&b&(2,3)c");
        List<Object> tokens = element.getTokens();
        Assert.assertEquals(tokens.size(), 5);
        Assert.assertEquals(tokens.get(0), "a");
        Assert.assertTrue(tokens.get(1) instanceof AmpReference);
        Assert.assertEquals(tokens.get(2), "b");
        Assert.assertTrue(tokens.get(3) instanceof AmpReference);
        Assert.assertEquals(tokens.get(4), "c");
        Assert.assertEquals(element.getCanonicalForm(), "a&(0,0)b&(2,3)c");
    }

    @Test
    public void testConstructorWithEmptyString() {
        AmpPathElement element = new AmpPathElement("");
        List<Object> tokens = element.getTokens();
        Assert.assertTrue(tokens.isEmpty());
        Assert.assertEquals(element.getCanonicalForm(), "");
    }

    @Test
    public void testEvaluateWithSingleReference() {
        AmpPathElement element = new AmpPathElement("&(1,2)");
        WalkedPath walkedPath = new WalkedPath();
        walkedPath.add(null, new MatchedElement("root"));
        walkedPath.add(null, new MatchedElement("photos-foo-bar", List.of("foo", "bar")));
        walkedPath.add(null, new MatchedElement("current"));
        Assert.assertEquals(element.evaluate(walkedPath), "bar");
    }
}
