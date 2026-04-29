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
import io.joltcommunity.jolt.traversr.SimpleTraversr;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * Parses one enrich spec entry and coordinates the work required to execute it.
 * <p>
 * A manager owns the input path matcher, output path resolver, and Java method invoker for a single
 * enrichment rule such as:
 * <pre>
 * {
 *   "path" : "customers.[*].id",
 *   "outputPath" : "customers.[*].profile",
 *   "contextKey" : "customerLookup",
 *   "method" : "enrich"
 * }
 * </pre>
 */
public class EnrichrManager {

    private final EnrichrMethodInvoker invoker;
    private final EnrichrPathTemplate inputPathTemplate;
    private final EnrichrPathTemplate outputPathTemplate;
    private final SimpleTraversr<Object> outputTraversr;

    /**
     * Parse one enrichment rule from the {@code enrichments} array.
     *
     * @param spec raw rule object
     * @param index position of the rule inside the spec, used for error messages
     */
    @SuppressWarnings( "unchecked" )
    public EnrichrManager( Object spec, int index ) {
        if ( ! ( spec instanceof Map ) ) {
            throw new SpecException( "Enrichr enrichment at index:" + index + " must be a Map." );
        }

        Map<String, Object> rule = (Map<String, Object>) spec;
        String path = requiredString( rule, "path", index );
        String outputPath = optionalString( rule, "outputPath", path );

        String methodName = requiredString( rule, "method", index );
        String contextKey = optionalString( rule, "contextKey", null );
        String className = optionalString( rule, "className", null );

        inputPathTemplate = EnrichrPathTemplate.parseInput( path, index );
        outputPathTemplate = EnrichrPathTemplate.parseOutput( outputPath, index );
        validateOutputPath( index );
        outputTraversr = outputPathTemplate.getTraversr();
        invoker = new EnrichrMethodInvoker( methodName, contextKey, className, index );
    }

    /**
     * Find every input value matched by this rule.
     * <p>
     * Fixed paths return at most one match, while wildcard array paths such as {@code customers.[*].id}
     * return one match per resolved array element.
     *
     * @param input document currently being transformed
     * @return resolved path matches for this rule
     */
    public List<EnrichrPathMatch> match( Object input ) {
        return inputPathTemplate.match( input );
    }

    /**
     * Create a pending enrichment for one already-resolved input match.
     * <p>
     * The method invocation is started immediately. The returned object is responsible for waiting on the
     * result and writing it back to the resolved output path later.
     *
     * @param inputMatch one resolved source value plus any wildcard bindings captured from the input path
     * @param input full input document
     * @param context optional transform context
     * @return pending enrichment ready to be applied
     */
    public EnrichrPendingEnrichment prepare( EnrichrPathMatch inputMatch, Object input, Map<String, Object> context ) {
        CompletionStage<Object> enrichedValueStage = invoker.invokeAsync( inputMatch.getValue(), input, context );
        List<String> outputKeys = outputPathTemplate.resolveKeys( inputMatch.getWildcardBindings() );
        String resolvedOutputPath = outputPathTemplate.resolvePath( inputMatch.getWildcardBindings() );
        return new EnrichrPendingEnrichment( input, outputTraversr, outputKeys, enrichedValueStage, resolvedOutputPath );
    }

    /**
     * Read a required non-blank string property from one enrichment rule.
     */
    static String requiredString( Map<String, Object> spec, String key, int index ) {
        Object value = spec.get( key );
        if ( ! ( value instanceof String ) || ( (String) value ).trim().isEmpty() ) {
            throw new SpecException( "Enrichr enrichment at index:" + index + " requires a non-blank '" + key + "'." );
        }
        return ( (String) value ).trim();
    }

    /**
     * Read an optional non-blank string property from one enrichment rule.
     */
    static String optionalString( Map<String, Object> spec, String key, String defaultValue ) {
        Object value = spec.get( key );
        if ( value == null ) {
            return defaultValue;
        }
        if ( ! ( value instanceof String ) || ( (String) value ).trim().isEmpty() ) {
            throw new SpecException( "Enrichr optional '" + key + "' must be a non-blank String when provided." );
        }
        return ( (String) value ).trim();
    }

    /**
     * Ensure the output path can be resolved for every wildcard captured from the input path.
     * <p>
     * Wildcard input paths must either bind the same number of {@code [*]} segments in the output path or
     * write to an append location using {@code []}.
     */
    private void validateOutputPath( int index ) {
        int inputWildcardCount = inputPathTemplate.getWildcardCount();
        int outputWildcardCount = outputPathTemplate.getWildcardCount();

        if ( outputWildcardCount > 0 && outputWildcardCount != inputWildcardCount ) {
            throw new SpecException(
                    "Enrichr enrichment at index:" + index +
                            " requires 'outputPath' to use the same number of '[*]' segments as 'path'."
            );
        }

        if ( inputWildcardCount > 0 && outputWildcardCount == 0 && ! outputPathTemplate.hasAppendSegment() ) {
            throw new SpecException(
                    "Enrichr enrichment at index:" + index +
                            " with wildcard 'path' requires 'outputPath' to either include matching '[*]' segments or use '[]' append semantics."
            );
        }
    }
}
