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
package io.joltcommunity.jolt.common;

import io.joltcommunity.jolt.common.pathelement.LiteralPathElement;
import io.joltcommunity.jolt.common.pathelement.PathElement;
import io.joltcommunity.jolt.exception.SpecException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PathElementBuilderTest {
    @Test(expectedExceptions = SpecException.class)
    public void testTooManyArrayBrackets() {
        PathElementBuilder.parseSingleKeyLHS("[foo][bar]");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testTooManyArrayBracketsWithEscapes() {
        PathElementBuilder.parseSingleKeyLHS("[foo\\][bar]");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testInvalidAtInMiddle() {
        PathElementBuilder.parseSingleKeyLHS("foo@bar");
    }

    public void testInvalidAtInMiddleWithEscapes() {
        PathElement pe = PathElementBuilder.parseSingleKeyLHS("foo\\@bar");
        Assert.assertTrue(pe instanceof LiteralPathElement);
    }

    @Test(expectedExceptions = SpecException.class)
    public void testMixStarAndAmp() {
        PathElementBuilder.parseSingleKeyLHS("foo*&bar");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testInvalidTransposeKey() {
        // Assuming TransposePathElement.parse throws SpecException for invalid keys
        PathElementBuilder.parseSingleKeyLHS("@(");
    }

    @Test(expectedExceptions = SpecException.class)
    public void testInvalidTransposeKeyWithPrefix() {
        PathElementBuilder.parseSingleKeyLHS("foo@bar");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullInput() {
        PathElementBuilder.parseSingleKeyLHS(null);
    }
}
