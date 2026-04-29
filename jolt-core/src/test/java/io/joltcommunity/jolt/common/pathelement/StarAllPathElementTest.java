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

import io.joltcommunity.jolt.exception.SpecException;
import org.testng.annotations.Test;

public class StarAllPathElementTest {
    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarAllPathElement key should just be a single '\\*'\\.  Was: foo")
    public void testNonStarKeyThrowsException() {
        new StarAllPathElement("foo");
    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarAllPathElement key should just be a single '\\*'\\.  Was: \\*\\*")
    public void testMultipleStarsThrowsException() {
        new StarAllPathElement("**");
    }

    @Test(expectedExceptions = SpecException.class,
            expectedExceptionsMessageRegExp = "StarAllPathElement key should just be a single '\\*'\\.  Was: ")
    public void testEmptyKeyThrowsException() {
        new StarAllPathElement("");
    }
}
