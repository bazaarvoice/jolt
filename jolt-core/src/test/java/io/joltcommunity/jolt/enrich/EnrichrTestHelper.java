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

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class EnrichrTestHelper {

    public static Object uppercase( Object value ) {
        return String.valueOf( value ).toUpperCase();
    }

    public static CompletionStage<Object> asyncUppercase( Object value ) {
        return CompletableFuture.completedFuture( uppercase( value ) );
    }

    public static Object describe( Object value, Object input, Map<String, Object> context ) {
        Map<String, Object> enriched = new LinkedHashMap<>();
        enriched.put( "original", value );
        enriched.put( "inputType", input == null ? null : input.getClass().getSimpleName() );
        enriched.put( "tenant", context == null ? null : context.get( "tenant" ) );
        return enriched;
    }

    public static Publisher<Object> publisherDescribe( Object value, Object input, Map<String, Object> context ) {
        return new SingleValuePublisher( describe( value, input, context ) );
    }

    public Object describeViaBean( Object value, Object input, Map<String, Object> context ) {
        return describe( value, input, context );
    }

    private static final class SingleValuePublisher implements Publisher<Object> {

        private final Object value;

        private SingleValuePublisher( Object value ) {
            this.value = value;
        }

        @Override
        public void subscribe( final Subscriber<? super Object> subscriber ) {
            subscriber.onSubscribe( new Subscription() {
                private boolean completed;

                @Override
                public void request( long n ) {
                    if ( completed ) {
                        return;
                    }
                    if ( n <= 0 ) {
                        completed = true;
                        subscriber.onError( new IllegalArgumentException( "Subscription request must be positive." ) );
                        return;
                    }

                    completed = true;
                    if ( value != null ) {
                        subscriber.onNext( value );
                    }
                    subscriber.onComplete();
                }

                @Override
                public void cancel() {
                    completed = true;
                }
            } );
        }
    }
}
