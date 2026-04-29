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

import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.reference.AmpReference;
import io.joltcommunity.jolt.common.reference.HashReference;
import io.joltcommunity.jolt.common.reference.PathAndGroupReference;
import io.joltcommunity.jolt.common.reference.PathReference;
import io.joltcommunity.jolt.common.tree.ArrayMatchedElement;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;

/**
 * Represents a path element for array access in a Jolt transformation specification.
 * Handles different array path types such as auto-expand, explicit index, references, hash, and transpose.
 * Provides evaluation and matching logic for array path elements.
 * <p>
 * It can be used on the Right hand sides of the spec only.
 */
public class ArrayPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final ArrayPathType arrayPathType;
    private final PathReference ref;
    private final TransposePathElement transposePathElement;
    private final String canonicalForm;
    private final String arrayIndex;

    public ArrayPathElement(String key) {
        super(key);

        if (key.charAt(0) != '[' || key.charAt(key.length() - 1) != ']') {
            throw new SpecException("Invalid ArrayPathElement key:" + key);
        }

        ArrayPathType apt;
        PathReference r = null;
        TransposePathElement tpe = null;
        String aI = "";

        if (key.length() == 2) {
            apt = ArrayPathType.AUTO_EXPAND;
            canonicalForm = "[]";
        } else {
            String meat = key.substring(1, key.length() - 1);  // trim the [ ]
            char firstChar = meat.charAt(0);

            if (AmpReference.TOKEN.equals(firstChar)) {
                r = new AmpReference(meat);
                apt = ArrayPathType.REFERENCE;
                canonicalForm = "[" + r.getCanonicalForm() + "]";
            } else if (HashReference.TOKEN.equals(firstChar)) {
                r = new HashReference(meat);
                apt = ArrayPathType.HASH;

                canonicalForm = "[" + r.getCanonicalForm() + "]";
            } else if ('@' == firstChar) {
                apt = ArrayPathType.TRANSPOSE;

                tpe = TransposePathElement.parse(meat);
                canonicalForm = "[" + tpe.getCanonicalForm() + "]";
            } else {
                aI = verifyStringIsNonNegativeInteger(meat);
                if (aI != null) {
                    apt = ArrayPathType.EXPLICIT_INDEX;
                    canonicalForm = "[" + aI + "]";
                } else {
                    throw new SpecException("Bad explicit array index:" + meat + " from key:" + key);
                }
            }
        }

        transposePathElement = tpe;
        arrayPathType = apt;
        ref = r;
        arrayIndex = aI;
    }

    /**
     * @return the String version of a non-Negative integer, else null
     */
    private static String verifyStringIsNonNegativeInteger(String key) {
        try {
            int number = Integer.parseInt(key);
            if (number >= 0) {
                return key;
            } else {
                return null;
            }
        } catch (NumberFormatException nfe) {
            // Jolt should not throw any exceptions just because the input data does not match what is expected.
            // Thus the exception is being swallowed.
            return null;
        }
    }

    @Override
    public String getCanonicalForm() {
        return canonicalForm;
    }

    @Override
    public String evaluate(WalkedPath walkedPath) {

        switch (arrayPathType) {
            case AUTO_EXPAND:
                return canonicalForm;

            case EXPLICIT_INDEX:
                return arrayIndex;

            case HASH:
                MatchedElement element = walkedPath.elementFromEnd(ref.getPathIndex()).getMatchedElement();
                return Integer.toString(element.getHashCount());

            case TRANSPOSE:
                String key = transposePathElement.evaluate(walkedPath);
                return verifyStringIsNonNegativeInteger(key);

            case REFERENCE:
                MatchedElement lpe = walkedPath.elementFromEnd(ref.getPathIndex()).getMatchedElement();
                String keyPart;

                if (ref instanceof PathAndGroupReference) {
                    keyPart = lpe.getSubKeyRef(((PathAndGroupReference) ref).getKeyGroup());
                } else {
                    keyPart = lpe.getSubKeyRef(0);
                }

                return verifyStringIsNonNegativeInteger(keyPart);
            default:
                throw new IllegalStateException("ArrayPathType enum added two without updating this switch statement.");
        }
    }

    public Integer getExplicitArrayIndex() {
        try {
            return Integer.parseInt(arrayIndex);
        } catch (Exception ignored) {
            return null;
        }
    }

    public boolean isExplicitArrayIndex() {
        return arrayPathType.equals(ArrayPathType.EXPLICIT_INDEX);
    }

    @Override
    public MatchedElement match(String dataKey, WalkedPath walkedPath) {
        String evaled = evaluate(walkedPath);
        if (evaled.equals(dataKey)) {
            Optional<Integer> origSizeOptional = walkedPath.lastElement().getOrigSize();
            if (origSizeOptional.isPresent()) {
                return new ArrayMatchedElement(evaled, origSizeOptional.get());
            } else {
                return null;
            }
        }
        return null;
    }

    public enum ArrayPathType {AUTO_EXPAND, REFERENCE, HASH, TRANSPOSE, EXPLICIT_INDEX}
}
