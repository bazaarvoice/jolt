/*
 * Copyright 2026 Jolt Community
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
package io.joltcommunity.jolt.enrich;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Concrete result of resolving one enrich input path against the current document.
 * <p>
 * For a fixed path this represents a single value. For wildcard array paths it represents one resolved
 * array element plus the wildcard bindings needed to compute the corresponding output path.
 */
public final class EnrichrPathMatch {

    private final Object value;
    private final List<String> resolvedInputKeys;
    private final List<String> wildcardBindings;
    private final String resolvedInputPath;

    /**
     * Capture one resolved input match.
     *
     * @param value matched input value
     * @param resolvedInputKeys traversr-friendly keys for the matched input path
     * @param wildcardBindings array indices captured from {@code [*]} segments
     * @param resolvedInputPath human-readable resolved path
     */
    EnrichrPathMatch( Object value, List<String> resolvedInputKeys, List<String> wildcardBindings, String resolvedInputPath ) {
        this.value = value;
        this.resolvedInputKeys = Collections.unmodifiableList( new ArrayList<>( resolvedInputKeys ) );
        this.wildcardBindings = Collections.unmodifiableList( new ArrayList<>( wildcardBindings ) );
        this.resolvedInputPath = resolvedInputPath;
    }

    /**
     * Return the value currently stored at the resolved input path.
     */
    Object getValue() {
        return value;
    }

    /**
     * Return the concrete input keys used to reach this match.
     */
    List<String> getResolvedInputKeys() {
        return resolvedInputKeys;
    }

    /**
     * Return the array indices captured from wildcard segments in the input path.
     */
    List<String> getWildcardBindings() {
        return wildcardBindings;
    }

    /**
     * Return the concrete input path in human-readable form.
     */
    String getResolvedInputPath() {
        return resolvedInputPath;
    }
}
