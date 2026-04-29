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

import io.joltcommunity.jolt.exception.SpecException;

/**
 * Execution strategy for {@link io.joltcommunity.jolt.Enrichr}.
 * <p>
 * {@code SYNC} applies each enrichment immediately after invocation.
 * {@code ASYNC} allows all invocations to start first and then blocks only when writing results back.
 */
public enum EnrichrExecutionMode {
    SYNC,
    ASYNC;

    private static final String EXECUTION_MODE_KEY = "executionMode";

    /**
     * Parse the optional {@code executionMode} spec field.
     *
     * @param rawValue raw value from the enrich spec
     * @return resolved execution mode, defaulting to {@code SYNC} when omitted
     */
    public static EnrichrExecutionMode fromSpec( Object rawValue ) {
        if ( rawValue == null ) {
            return SYNC;
        }
        if ( ! ( rawValue instanceof String ) ) {
            throw new SpecException( "Enrichr optional '" + EXECUTION_MODE_KEY + "' must be a String when provided." );
        }

        String normalizedValue = ( (String) rawValue ).trim();
        if ( normalizedValue.isEmpty() ) {
            throw new SpecException( "Enrichr optional '" + EXECUTION_MODE_KEY + "' must not be blank when provided." );
        }
        if ( "sync".equalsIgnoreCase( normalizedValue ) ) {
            return SYNC;
        }
        if ( "async".equalsIgnoreCase( normalizedValue ) ) {
            return ASYNC;
        }

        throw new SpecException( "Enrichr optional '" + EXECUTION_MODE_KEY + "' must be either 'sync' or 'async'." );
    }
}
