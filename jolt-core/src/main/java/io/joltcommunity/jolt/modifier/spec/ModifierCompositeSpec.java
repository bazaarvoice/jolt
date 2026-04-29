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

import io.joltcommunity.jolt.common.ComputedKeysComparator;
import io.joltcommunity.jolt.common.ExecutionStrategy;
import io.joltcommunity.jolt.common.Optional;
import io.joltcommunity.jolt.common.pathelement.*;
import io.joltcommunity.jolt.common.spec.BaseSpec;
import io.joltcommunity.jolt.common.spec.OrderedCompositeSpec;
import io.joltcommunity.jolt.common.tree.ArrayMatchedElement;
import io.joltcommunity.jolt.common.tree.MatchedElement;
import io.joltcommunity.jolt.common.tree.WalkedPath;
import io.joltcommunity.jolt.exception.SpecException;
import io.joltcommunity.jolt.modifier.DataType;
import io.joltcommunity.jolt.modifier.OpMode;
import io.joltcommunity.jolt.modifier.ModifierSpecBuilder;

import java.util.*;

/**
 * Composite spec is non-leaf level spec that contains one or many child specs and processes
 * them based on a pre-determined execution strategy
 */
public class ModifierCompositeSpec extends ModifierSpec implements OrderedCompositeSpec {
    private static final HashMap<Class, Integer> orderMap;
    private static final ComputedKeysComparator computedKeysComparator;

    static {
        orderMap = new HashMap<>();
        orderMap.put(ArrayPathElement.class, 1);
        orderMap.put(StarRegexPathElement.class, 2);
        orderMap.put(StarDoublePathElement.class, 3);
        orderMap.put(StarSinglePathElement.class, 4);
        orderMap.put(StarAllPathElement.class, 5);
        computedKeysComparator = ComputedKeysComparator.fromOrder(orderMap);
    }

    private final Map<String, ModifierSpec> literalChildren;
    private final List<ModifierSpec> computedChildren;
    private final ExecutionStrategy executionStrategy;
    private final DataType specDataType;

    public ModifierCompositeSpec(final String key, final Map<String, Object> spec, final OpMode opMode, ModifierSpecBuilder specBuilder) {
        super(key, opMode);

        Map<String, ModifierSpec> literals = new LinkedHashMap<>();
        ArrayList<ModifierSpec> computed = new ArrayList<>();

        List<ModifierSpec> children = specBuilder.createSpec(spec);

        // remember max explicit index from spec to expand input array at runtime
        // need to validate spec such that it does not specify both array and literal path element
        int maxExplicitIndexFromSpec = -1, confirmedMapAtIndex = -1, confirmedArrayAtIndex = -1;

        for (int i = 0; i < children.size(); i++) {
            ModifierSpec childSpec = children.get(i);
            PathElement childPathElement = childSpec.pathElement;

            // for every child,
            //  a) mark current index as either must be map or must be array
            //  b) mark it as literal or computed
            //  c) if arrayPathElement,
            //      - make sure its an explicit index type
            //      - save the max explicit index in spec
            if (childPathElement instanceof LiteralPathElement) {
                confirmedMapAtIndex = i;
                literals.put(childPathElement.getRawKey(), childSpec);
            } else if (childPathElement instanceof ArrayPathElement childArrayPathElement) {
                confirmedArrayAtIndex = i;

                if (!childArrayPathElement.isExplicitArrayIndex()) {
                    throw new SpecException(opMode.name() + " RHS only supports explicit Array path element");
                }
                int explicitIndex = childArrayPathElement.getExplicitArrayIndex();
                // if explicit index from spec also enforces "[...]?" don't bother using that as max index
                if (!childSpec.checkValue) {
                    maxExplicitIndexFromSpec = Math.max(maxExplicitIndexFromSpec, explicitIndex);
                }

                literals.put(String.valueOf(explicitIndex), childSpec);
            } else {
                // StarPathElements evaluates to String keys in a Map, EXCEPT StarAllPathElement
                // which can be both all keys in a map or all indexes in a list
                if (!(childPathElement instanceof StarAllPathElement)) {
                    confirmedMapAtIndex = i;
                }
                computed.add(childSpec);
            }

            // Bail as soon as both confirmedMapAtIndex & confirmedArrayAtIndex is set
            if (confirmedMapAtIndex > -1 && confirmedArrayAtIndex > -1) {
                throw new SpecException(opMode.name() + " RHS cannot mix int array index and string map key, defined spec for " + key + " contains: " + children.get(confirmedMapAtIndex).pathElement.getCanonicalForm() + " conflicting " + children.get(confirmedArrayAtIndex).pathElement.getCanonicalForm());
            }
        }

        // set the dataType from calculated indexes
        specDataType = DataType.determineDataType(confirmedArrayAtIndex, confirmedMapAtIndex, maxExplicitIndexFromSpec);

        // Only the computed children need to be sorted
        computed.sort(computedKeysComparator);

        computed.trimToSize();

        literalChildren = Collections.unmodifiableMap(literals);
        computedChildren = Collections.unmodifiableList(computed);

        // extract generic execution strategy
        executionStrategy = determineExecutionStrategy();

    }

    @Override
    @SuppressWarnings("unchecked")
    public void applyElement(final String inputKey, Optional<Object> inputOptional, MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context) {

        Object input = inputOptional.get();
        // sanity checks, cannot work on a list spec with map input and vice versa, and runtime with null input
        if (!specDataType.isCompatible(input)) {
            return;
        }

        // create input if it is null
        if (input == null) {
            input = specDataType.create(inputKey, walkedPath, opMode);
            // if input has changed, wrap
            if (input != null) {
                inputOptional = Optional.of(input);
            }
        }

        // if input is List, create special ArrayMatchedElement, which tracks the original size of the input array
        if (input instanceof List) {
            // LIST means spec had array index explicitly specified, hence expand if needed
            if (specDataType instanceof DataType.LIST) {
                int origSize = specDataType.expand(input);
                thisLevel = new ArrayMatchedElement(thisLevel.getRawKey(), origSize);
            } else {
                // specDataType is RUNTIME, so spec had no array index explicitly specified, no need to expand
                thisLevel = new ArrayMatchedElement(thisLevel.getRawKey(), ((List<?>) input).size());
            }
        }

        // add self to walked path
        walkedPath.add(input, thisLevel);
        // Handle the rest of the children
        executionStrategy.process(this, inputOptional, walkedPath, null, context);
        // We are done, so remove ourselves from the walkedPath
        walkedPath.removeLastElement();
    }

    @Override
    public Map<String, ? extends BaseSpec> getLiteralChildren() {
        return literalChildren;
    }

    @Override
    public List<? extends BaseSpec> getComputedChildren() {
        return computedChildren;
    }

    @Override
    public ExecutionStrategy determineExecutionStrategy() {

        if (computedChildren.isEmpty()) {
            return ExecutionStrategy.ALL_LITERALS;
        } else if (literalChildren.isEmpty()) {
            return ExecutionStrategy.COMPUTED;
        } else if (opMode.equals(OpMode.DEFINER) && specDataType instanceof DataType.LIST) {
            return ExecutionStrategy.CONFLICT;
        } else {
            return ExecutionStrategy.ALL_LITERALS_WITH_COMPUTED;
        }
    }
}
