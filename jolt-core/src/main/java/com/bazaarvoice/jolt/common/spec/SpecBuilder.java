/*
 * Copyright 2013 Bazaarvoice, Inc.
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

package com.bazaarvoice.jolt.common.spec;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory class that provides a factory method create(...) that takes itself
 * as argument to specify how to handle child specs
 *
 * @param <T>
 */
public abstract class SpecBuilder<T extends BaseSpec> {

    /**
     * Recursively walk the spec input tree.
     */
    public List<T> createSpec(Map<String, Object> rawSpec) {
        List<T> result = new ArrayList<>();
        Set<String> actualKeys = new HashSet<>();

        for ( String rawLhsStr : rawSpec.keySet() ) {

            Object rawRhs = rawSpec.get( rawLhsStr );
            String[] keyStrings = rawLhsStr.split( "\\|" ); // unwrap the syntactic sugar of the OR
            for ( String keyString : keyStrings ) {

                T childSpec = createSpec( keyString, rawRhs );

                String childCanonicalString = childSpec.getPathElement().getCanonicalForm();

                if ( actualKeys.contains( childCanonicalString ) ) {
                    throw new IllegalArgumentException( "Duplicate canonical key found : " + childCanonicalString );
                }

                actualKeys.add( childCanonicalString );

                result.add(childSpec);
            }
        }

        return result;
    }

    /**
     * Given a lhs key and rhs spec object, determine, create and return appropriate spec
     * @param lhsKey lhs key
     * @param rhsSpec rhs Spec
     * @return Spec object
     */
    public abstract T createSpec( String lhsKey, Object rhsSpec );
}
