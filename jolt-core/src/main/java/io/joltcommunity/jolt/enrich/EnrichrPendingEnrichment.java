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

import io.joltcommunity.jolt.exception.TransformException;
import io.joltcommunity.jolt.traversr.SimpleTraversr;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * Represents an enrichment invocation that has already been started but not yet written back to the
 * document.
 * <p>
 * This is used by both sync and async execution paths so invocation and document mutation remain cleanly
 * separated.
 */
public class EnrichrPendingEnrichment {

    private final Object input;
    private final SimpleTraversr<Object> outputTraversr;
    private final List<String> outputKeys;
    private final CompletionStage<Object> enrichedValueStage;
    private final String outputPath;

    /**
     * Create a pending write-back operation for one resolved enrichment match.
     */
    EnrichrPendingEnrichment(
            Object input,
            SimpleTraversr<Object> outputTraversr,
            List<String> outputKeys,
            CompletionStage<Object> enrichedValueStage,
            String outputPath
    ) {
        this.input = input;
        this.outputTraversr = outputTraversr;
        this.outputKeys = outputKeys;
        this.enrichedValueStage = enrichedValueStage;
        this.outputPath = outputPath;
    }

    /**
     * Wait for the enrichment result and write it to the resolved output path.
     */
    public void apply() {
        Object enrichedValue = resolveValue( enrichedValueStage, outputPath );
        outputTraversr.set( input, outputKeys, enrichedValue );
    }

    /**
     * Resolve the asynchronous result into a concrete value while preserving interruption semantics.
     */
    private static Object resolveValue( CompletionStage<Object> enrichedValueStage, String outputPath ) {
        try {
            return enrichedValueStage.toCompletableFuture().get();
        }
        catch ( InterruptedException e ) {
            Thread.currentThread().interrupt();
            throw new TransformException( "Enrichr asynchronous enrichment was interrupted for outputPath '" + outputPath + "'.", e );
        }
        catch ( ExecutionException e ) {
            Throwable cause = e.getCause();
            if ( cause instanceof RuntimeException ) {
                throw (RuntimeException) cause;
            }
            throw new TransformException( "Enrichr asynchronous enrichment failed for outputPath '" + outputPath + "'.", cause );
        }
    }
}
