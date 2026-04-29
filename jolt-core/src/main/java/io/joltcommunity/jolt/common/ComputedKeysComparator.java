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

import io.joltcommunity.jolt.common.pathelement.PathElement;
import io.joltcommunity.jolt.common.spec.BaseSpec;

import java.util.Comparator;
import java.util.HashMap;

/**
 * This Comparator is used for determining the execution order of childSpecs.apply(...)
 * <p>
 * Argument Map of Class: integer is used to determine precedence
 */
public class ComputedKeysComparator implements Comparator<BaseSpec> {

    private final HashMap<Class, Integer> orderMap;

    private ComputedKeysComparator(HashMap<Class, Integer> orderMap) {
        this.orderMap = orderMap;
    }

    /**
     * Static factory method to get an Comparator instance for a given order map
     *
     * @param orderMap of precedence
     * @return Comparator that uses the given order map to determine precedence
     */
    public static ComputedKeysComparator fromOrder(HashMap<Class, Integer> orderMap) {
        return new ComputedKeysComparator(orderMap);
    }

    @Override
    public int compare(BaseSpec a, BaseSpec b) {

        PathElement ape = a.getPathElement();
        PathElement bpe = b.getPathElement();

        int aa = orderMap.get(ape.getClass());
        int bb = orderMap.get(bpe.getClass());

        int elementsEqual = Integer.compare(aa, bb);

        if (elementsEqual != 0) {
            return elementsEqual;
        }

        // At this point we have two PathElements of the same type.
        String acf = ape.getCanonicalForm();
        String bcf = bpe.getCanonicalForm();

        int alen = acf.length();
        int blen = bcf.length();

        // Sort them by length, with the longest (most specific) being first
        //  aka "rating-range-*" needs to be evaluated before "rating-*", or else "rating-*" will catch too much
        // If the lengths are equal, sort alphabetically as the last ditch deterministic behavior
        return alen > blen ? -1 : alen == blen ? acf.compareTo(bcf) : 1;
    }
}
