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

import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.PathStep;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class TransposePathElementTest {

    @Test
    public void testParseSimpleKey() {
        TransposePathElement element = TransposePathElement.parse("@author");
        assertEquals(element.getCanonicalForm(), "@(0,author)");
    }

    @Test
    public void testParseNumericKey() {
        TransposePathElement element = TransposePathElement.parse("@2");
        assertEquals(element.getCanonicalForm(), "@(2,)");
    }

    @Test
    public void testParseKeyWithSubPath() {
        TransposePathElement element = TransposePathElement.parse("@2,book");
        assertEquals(element.getCanonicalForm(), "@(2,book)");
    }

    @Test
    public void testParseKeyWithParens() {
        TransposePathElement element = TransposePathElement.parse("@(author)");
        assertEquals(element.getCanonicalForm(), "@(0,author)");
    }

    @Test
    public void testParseKeyWithEscape() {
        TransposePathElement element = TransposePathElement.parse("@(a.b\\.c)");
        assertEquals(element.getCanonicalForm(), "@(0,a.b\\.c)");
    }

    @Test
    public void testParseKeyWithAmpPathReference() {
        TransposePathElement element = TransposePathElement.parse("@(a.&2.c)");
        assertEquals(element.getCanonicalForm(), "@(0,a.&(2,0).c)");
    }

    @Test
    public void testParseKeyWithAmpPathReferenceDirectly() {
        TransposePathElement element = TransposePathElement.parse("@&1");
        assertEquals(element.getCanonicalForm(), "@(0,&(1,0))");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testParseInvalidKeyThrowsException() {
        TransposePathElement.parse("@");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testParseKeyWithNestedAtThrowsException() {
        TransposePathElement.parse("@author@book");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testParseKeyWithWildcardThrowsException() {
        TransposePathElement.parse("@author*");
    }

    @Test
    public void testEvaluateReturnsNullForNonString() {
        Date date = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("date", date);
        WalkedPath walkedPath = new WalkedPath();
        PathStep pathStep = new PathStep(map, new MatchedElement("date"));
        walkedPath.add(pathStep);
        TransposePathElement pe = TransposePathElement.parse("@date");
        String result = pe.evaluate(walkedPath);
        assertNull(result, "Expected null for non-string data type");
    }
}
