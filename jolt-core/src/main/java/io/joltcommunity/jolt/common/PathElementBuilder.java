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

package io.joltcommunity.jolt.common;

import io.joltcommunity.jolt.common.pathelement.*;
import io.joltcommunity.jolt.exception.SpecException;
import io.joltcommunity.jolt.utils.StringTools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.joltcommunity.jolt.common.SpecStringParser.*;

/**
 * Static utility class that creates PathElement(s) given a string key from a json spec document
 */
public class PathElementBuilder {

    private PathElementBuilder() {
    }

    /**
     * Create a path element and ensures it is a Matchable Path Element
     */
    public static MatchablePathElement buildMatchablePathElement(String rawJsonKey) {
        PathElement pe = PathElementBuilder.parseSingleKeyLHS(rawJsonKey);

        if (!(pe instanceof MatchablePathElement)) {
            throw new SpecException("Spec LHS key=" + rawJsonKey + " is not a valid LHS key.");
        }

        return (MatchablePathElement) pe;
    }

    /**
     * Visible for Testing.
     * <p>
     * Inspects the key in a particular order to determine the correct subclass of
     * PathElement to create.
     *
     * @param origKey String that should represent a single PathElement
     * @return a concrete implementation of PathElement
     */
    public static PathElement parseSingleKeyLHS(String origKey) {
        return parseKeyToPathElement(origKey);
    }

    /**
     * Parse the dotNotation of the RHS.
     */
    public static List<PathElement> parseDotNotationRHS(String dotNotation) {
        String fixedNotation = fixLeadingBracketSugar(dotNotation);
        List<String> pathStrs = parseDotNotation(new LinkedList<>(), stringIterator(fixedNotation), dotNotation);

        return parseKeysToPathElements(pathStrs, dotNotation);
    }

    /**
     * @param refDotNotation the original dotNotation string used for error messages
     * @return List of PathElements based on the provided List<String> keys
     */
    public static List<PathElement> parseKeysToPathElements(List<String> keys, String refDotNotation) {
        ArrayList<PathElement> paths = new ArrayList<>();

        for (String key : keys) {
            PathElement path = parseKeyToPathElement(key);
            if (path instanceof AtPathElement) {
                throw new SpecException("'.@.' is not valid on the RHS: " + refDotNotation);
            }
            paths.add(path);
        }

        return paths;
    }

    private static PathElement parseKeyToPathElement(String origKey) {
        String elementKey = origKey;  // the String to use to actually make Elements
        String keyToInspect = origKey;  // the String to use to determine which kind of Element to create

        if (origKey.contains("\\")) {
            // only do the extra work of processing for escaped chars, if there is one.
            keyToInspect = removeEscapedValues(origKey);
            elementKey = removeEscapeChars(origKey);
        }

        if ("@".equals(keyToInspect)) {
            return new AtPathElement(elementKey);
        } else if ("*".equals(keyToInspect)) {
            return new StarAllPathElement(elementKey);
        } else if (keyToInspect.startsWith("$")) {
            return new DollarPathElement(elementKey);
        } else if (keyToInspect.startsWith("#")) {
            return new HashPathElement(elementKey);
        } else if (keyToInspect.startsWith("[")) {

            if (StringTools.countMatches(keyToInspect, "[") != 1 || StringTools.countMatches(keyToInspect, "]") != 1) {
                throw new SpecException("Invalid key:" + origKey + " has too many [] references.");
            }

            return new ArrayPathElement(elementKey);
        }
        else if (keyToInspect.startsWith("@")) {
            // The transpose path element gets the origKey so that it has it's escapes.
            return TransposePathElement.parse(origKey);
        } else if (keyToInspect.contains("@")) {
            throw new SpecException("Invalid key:" + origKey + " can not have an @ other than at the front.");
        } else if (keyToInspect.contains("&")) {

            if (keyToInspect.contains("*")) {
                throw new SpecException("Invalid key:" + origKey + ", Can't mix * with & ) ");
            }

            return new AmpPathElement(elementKey);
        } else if (keyToInspect.contains("*")) {
            return parseStarPathElement(keyToInspect, elementKey);
        }
        return new LiteralPathElement(elementKey);
    }

    private static StarPathElement parseStarPathElement(String keyToInspect, String elementKey) {
        int numOfStars = StringTools.countMatches(keyToInspect, "*");

        if (numOfStars == 1) {
            return new StarSinglePathElement(elementKey);
        } else if (numOfStars == 2) {
            return new StarDoublePathElement(elementKey);
        }
        return new StarRegexPathElement(elementKey);
    }
}
