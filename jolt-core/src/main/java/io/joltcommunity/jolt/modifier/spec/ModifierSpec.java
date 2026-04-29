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

package io.joltcommunity.jolt.modifier.spec;

import io.joltcommunity.jolt.SpecDriven;
import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.PathEvaluatingTraversal;
import io.joltcommunity.jolt.common.TransposeReader;
import io.joltcommunity.jolt.common.TraversalBuilder;
import io.joltcommunity.jolt.common.pathelement.ArrayPathElement;
import io.joltcommunity.jolt.common.pathelement.LiteralPathElement;
import io.joltcommunity.jolt.common.pathelement.MatchablePathElement;
import io.joltcommunity.jolt.common.pathelement.StarPathElement;
import io.joltcommunity.jolt.common.spec.BaseSpec;
import io.joltcommunity.jolt.common.tree.ArrayMatchedElement;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;
import io.joltcommunity.jolt.exception.TransformException;
import io.joltcommunity.jolt.modifier.OpMode;

import java.util.List;
import java.util.Map;

import static io.joltcommunity.jolt.common.PathElementBuilder.buildMatchablePathElement;


/**
 * Base Modifier spec
 */
public abstract class ModifierSpec implements BaseSpec {

    // traversal builder that uses a TransposeReader to create a PathEvaluatingTraversal
    protected static final TraversalBuilder TRAVERSAL_BUILDER = new TraversalBuilder() {
        @Override
        @SuppressWarnings("unchecked")
        public <T extends PathEvaluatingTraversal> T buildFromPath(final String path) {
            return (T) new TransposeReader(path);
        }
    };

    protected final OpMode opMode;
    protected final MatchablePathElement pathElement;
    protected final boolean checkValue;

    /**
     * Constructor for ModifierSpec.
     * Builds the left-hand side (LHS) path element and validates it against the specification.
     *
     * @param rawJsonKey The raw JSON key to process.
     * @param opMode The operation mode {@link OpMode} to use.
     * @throws SpecException If the path element is invalid for the given operation mode.
     */
    protected ModifierSpec(String rawJsonKey, OpMode opMode) {
        String prefix = rawJsonKey.substring(0, 1);
        String suffix = rawJsonKey.length() > 1 ? rawJsonKey.substring(rawJsonKey.length() - 1) : null;

        if (OpMode.isValid(prefix)) {
            this.opMode = OpMode.from(prefix);
            rawJsonKey = rawJsonKey.substring(1);
        } else {
            this.opMode = opMode;
        }

        if (suffix != null && suffix.equals("?") && !(rawJsonKey.endsWith("\\?"))) {
            checkValue = true;
            rawJsonKey = rawJsonKey.substring(0, rawJsonKey.length() - 1);
        } else {
            checkValue = false;
        }

        this.pathElement = buildMatchablePathElement(rawJsonKey);
        if (!(pathElement instanceof StarPathElement) && !(pathElement instanceof LiteralPathElement) && !(pathElement instanceof ArrayPathElement)) {
            throw new SpecException(opMode.name() + " cannot have " + pathElement.getClass().getSimpleName() + " RHS");
        }
    }

    /**
     * Static utility method for facilitating writes on the input object.
     *
     * @param parent The source object (either a Map or List).
     * @param matchedElement The current spec (leaf) element that was matched with the input.
     * @param value The value to write.
     * @param opMode The operation mode to determine if the write is applicable.
     * @throws RuntimeException If the parent object is neither a Map nor a List.
     */
    @SuppressWarnings("unchecked")
    protected static void setData(Object parent, MatchedElement matchedElement, Object value, OpMode opMode) {
        if (parent instanceof Map source) {
            String key = matchedElement.getRawKey();
            if (opMode.isApplicable(source, key)) {
                source.put(key, value);
            }
        } else if (parent instanceof List source && matchedElement instanceof ArrayMatchedElement) {
            int origSize = ((ArrayMatchedElement) matchedElement).getOrigSize();
            int reqIndex = ((ArrayMatchedElement) matchedElement).getRawIndex();
            if (opMode.isApplicable(source, reqIndex, origSize)) {
                source.set(reqIndex, value);
            }
        } else {
            throw new RuntimeException("Should not come here!");
        }
    }

    @Override
    public MatchablePathElement getPathElement() {
        return pathElement;
    }

    @Override
    public boolean apply(final String inputKey, final Optional<Object> inputOptional, final WalkedPath walkedPath, final Map<String, Object> output, final Map<String, Object> context) {
        if (output != null) {
            throw new TransformException("Expected a null output");
        }

        MatchedElement thisLevel = pathElement.match(inputKey, walkedPath);
        if (thisLevel == null) {
            return false;
        }

        if (!checkValue) { // there was no trailing "?" so no check is necessary
            applyElement(inputKey, inputOptional, thisLevel, walkedPath, context);
        } else if (inputOptional.isPresent()) {
            applyElement(inputKey, inputOptional, thisLevel, walkedPath, context);
        }
        return true;
    }

    /**
     * Modifier specific override that is used in BaseSpec#apply(...)
     * The name is changed for easy identification during debugging
     */
    protected abstract void applyElement(final String key, final Optional<Object> inputOptional, final MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context);
}
