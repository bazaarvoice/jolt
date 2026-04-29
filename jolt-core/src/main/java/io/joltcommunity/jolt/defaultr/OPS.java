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
package io.joltcommunity.jolt.defaultr;

import io.joltcommunity.jolt.Defaultr;
import io.joltcommunity.jolt.exception.SpecException;

import java.util.Comparator;
import java.util.EnumMap;

public enum OPS {

    STAR, OR, LITERAL;

    private static final EnumMap<OPS, Integer> precedenceMap = new EnumMap<>(OPS.class);

    static {
        precedenceMap.put(LITERAL, 1);
        precedenceMap.put(OR, 2);
        precedenceMap.put(STAR, 3);
    }

    public static OPS parse(String key) {
        if (key.contains(Defaultr.WildCards.STAR)) {

            if (!Defaultr.WildCards.STAR.equals(key)) {
                throw new SpecException("Defaultr key " + key + " is invalid.  * keys can only contain *, and no other characters.");
            }

            return STAR;
        }
        if (key.contains(Defaultr.WildCards.OR)) {
            return OR;
        }
        return LITERAL;
    }

    public static class OpsPrecedenceComparator implements Comparator<OPS> {
        @Override
        public int compare(OPS ops1, OPS ops2) {
            return Integer.compare(precedenceMap.get(ops1), precedenceMap.get(ops2));
        }
    }
}
