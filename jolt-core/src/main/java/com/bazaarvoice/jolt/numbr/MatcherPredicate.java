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
package com.bazaarvoice.jolt.numbr;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Base class for the predicates used to determine if a key is a match.  Ordering is:
 *
 * - Exact string match (ties determined by string)
 * - Wildcard match (ties determined by wildcard string)
 * - All matcher
 */
abstract public class MatcherPredicate implements Comparable<MatcherPredicate> {

    private final static AllMatcher _allMatcherInstance = new AllMatcher();

    public static List<MatcherPredicate> extractMatcherPredicates(String mapSpecKey) {
        List<MatcherPredicate> matchers;
        if ("*".equals(mapSpecKey)) {
            matchers = new ArrayList<>(1);
            matchers.add(_allMatcherInstance);
        } else {
            String[] keyParts = mapSpecKey.split("\\|");
            matchers = new ArrayList<>(keyParts.length);
            for (String keyPart : keyParts) {
                if (keyPart.contains("*")) {
                    matchers.add(new WildcardMatcher(keyPart));
                } else {
                    matchers.add(new ExactMatcher(keyPart));
                }
            }
        }

        return matchers;
    }

    abstract public boolean matches(String key);

    /**
     * Matcher for all values.
     */
    private static class AllMatcher extends MatcherPredicate {
        @Override
        public boolean matches(String key) {
            return true;
        }

        @Override
        public int compareTo(MatcherPredicate o) {
            return o instanceof AllMatcher ? 0 : 1;
        }
    }

    /**
     * Matcher to an exact String.
     */
    private static class ExactMatcher extends MatcherPredicate {
        private final String _value;

        private ExactMatcher(String value) {
            _value = value;
        }

        @Override
        public boolean matches(String key) {
            return _value.equals(key);
        }

        @Override
        public int compareTo(MatcherPredicate o) {
            return o instanceof ExactMatcher ? _value.compareTo(((ExactMatcher) o)._value) : -1;
        }
    }

    /**
     * Matcher that creates a regex based on the wildcards in the key.
     */
    private static class WildcardMatcher extends MatcherPredicate {
        private final Pattern _regex;

        private WildcardMatcher(String pattern) {
            StringBuilder convertedPattern = new StringBuilder();
            int i = 0;
            int wc;
            while (i < pattern.length() && (wc = pattern.indexOf('*', i)) != -1) {
                if (wc != i) {
                    convertedPattern.append(Pattern.quote(pattern.substring(i, wc)));
                }
                convertedPattern.append(".*");
                i = wc + 1;
            }
            if (i < pattern.length()) {
                convertedPattern.append(pattern.substring(i));
            }
            _regex = Pattern.compile(convertedPattern.toString());
        }

        @Override
        public boolean matches(String key) {
            return _regex.matcher(key).matches();
        }

        @Override
        public int compareTo(MatcherPredicate o) {
            if (o instanceof ExactMatcher) {
                return 1;
            } else if (o instanceof AllMatcher) {
                return -1;
            } else {
                return _regex.pattern().compareTo(((WildcardMatcher) o)._regex.pattern());
            }
        }
    }
}
