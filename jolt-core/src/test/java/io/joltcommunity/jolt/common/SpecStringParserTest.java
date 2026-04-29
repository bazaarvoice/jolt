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

import io.joltcommunity.jolt.exception.SpecException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SpecStringParserTest {
    @Test
    public void testSimpleDotNotation() {
        String input = "foo.bar.baz";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo", "bar", "baz"));
    }

    @Test
    public void testEscapedDot() {
        String input = "foo\\.bar.baz";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo.bar", "baz"));
    }

    @Test
    public void testEscapedEscape() {
        String input = "foo\\\\bar.baz";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo\\bar", "baz"));
    }

    @Test
    public void testAtPathElement() {
        String input = "foo.@(bar.baz).qux";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo", "@(bar.baz)", "qux"));
    }

    @Test
    public void testEscapedAt() {
        String input = "foo.\\@bar.baz";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo", "\\@bar", "baz"));
    }

    @Test
    public void testEmptyInput() {
        String input = "";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testTrailingDot() {
        String input = "foo.bar.";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo", "bar"));
    }

    @Test
    public void testLeadingDot() {
        String input = ".foo.bar";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo", "bar"));
    }

    @Test
    public void testBracketedAt() {
        String input = "foo.@(bar.[baz]).qux";
        List<String> result = SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
        Assert.assertEquals(result, List.of("foo", "@(bar.[baz])", "qux"));
    }

    @Test(expectedExceptions = SpecException.class)
    public void testInvalidAtPathElementThrowsException() {
        // Invalid: "@." is not a valid AtPathElement
        String input = "foo.@.bar";
        SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
    }

    @Test(expectedExceptions = SpecException.class)
    public void testUnmatchedParenthesisThrowsException() {
        // Invalid: "@(bar.baz" has unmatched parenthesis
        String input = "foo.@(bar.baz.qux";
        SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(input),
                input
        );
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testNullInputThrowsException() {
        SpecStringParser.parseDotNotation(
                new ArrayList<>(),
                SpecStringParser.stringIterator(null),
                null
        );
    }
}
