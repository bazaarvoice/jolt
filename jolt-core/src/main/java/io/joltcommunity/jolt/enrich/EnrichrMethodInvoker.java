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
import io.joltcommunity.jolt.exception.TransformException;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves and invokes the Java method configured for one enrich rule.
 * <p>
 * Targets can come either from a declared {@code className} or from a runtime {@code contextKey}. Return
 * values are normalized to {@link CompletionStage} so the rest of the enrich flow can treat sync,
 * {@link CompletionStage}, and reactive {@link Publisher} methods uniformly.
 */
final class EnrichrMethodInvoker {

    private final Method method;
    private final Object target;
    private final String methodName;
    private final String contextKey;
    private final int index;
    private final Map<Class<?>, Method> contextMethodCache;

    /**
     * Create an invoker for one enrich rule.
     *
     * @param methodName public method to invoke
     * @param contextKey optional context map key that supplies the target instance
     * @param className optional fully qualified class name used to resolve the target up front
     * @param index enrich rule index used in validation messages
     */
    EnrichrMethodInvoker( String methodName, String contextKey, String className, int index ) {
        this.methodName = methodName;
        this.index = index;
        this.contextKey = contextKey;
        this.contextMethodCache = new ConcurrentHashMap<>();

        if ( className == null && contextKey == null ) {
            throw new SpecException( "Enrichr enrichment at index:" + index + " requires either 'className' or 'contextKey'." );
        }
        if ( className != null && contextKey != null ) {
            throw new SpecException( "Enrichr enrichment at index:" + index + " supports only one of 'className' or 'contextKey'." );
        }

        if ( className == null ) {
            method = null;
            target = null;
            return;
        }

        try {
            Class<?> clazz = Class.forName( className );
            method = findMethod( clazz, this.methodName, index );

            if ( java.lang.reflect.Modifier.isStatic( method.getModifiers() ) ) {
                target = null;
            }
            else {
                target = clazz.getDeclaredConstructor().newInstance();
            }
        }
        catch ( SpecException e ) {
            throw e;
        }
        catch ( Exception e ) {
            throw new SpecException( "Enrichr could not initialize enrichment at index:" + index + ".", e );
        }
    }

    /**
     * Invoke the configured method and normalize the result to a completion stage.
     *
     * @param value matched input value
     * @param input full input document
     * @param context optional transform context
     * @return a completion stage representing the final enriched value
     */
    CompletionStage<Object> invokeAsync( Object value, Object input, Map<String, Object> context ) {
        try {
            InvocationTarget invocationTarget = resolveInvocationTarget( context );
            Class<?>[] parameterTypes = invocationTarget.method.getParameterTypes();
            Object invocationResult;

            if ( parameterTypes.length == 1 ) {
                invocationResult = invocationTarget.method.invoke( invocationTarget.target, value );
            }
            else if ( parameterTypes.length == 2 ) {
                invocationResult = invocationTarget.method.invoke( invocationTarget.target, value, input );
            }
            else {
                invocationResult = invocationTarget.method.invoke( invocationTarget.target, value, input, context );
            }

            return toCompletionStage( invocationResult );
        }
        catch ( IllegalAccessException | InvocationTargetException e ) {
            Throwable cause = e instanceof InvocationTargetException ? ( (InvocationTargetException) e ).getCause() : e;
            throw new TransformException( "Enrichr failed invoking " + methodName + ".", cause );
        }
    }

    /**
     * Resolve the concrete method target for this invocation.
     * <p>
     * Class-based rules are resolved once in the constructor. Context-based rules are resolved at runtime so
     * the context map can supply request-scoped collaborators such as Spring beans.
     */
    private InvocationTarget resolveInvocationTarget( Map<String, Object> context ) {
        if ( method != null ) {
            return new InvocationTarget( method, target );
        }

        if ( context == null ) {
            throw new TransformException( "Enrichr could not resolve contextKey '" + contextKey + "' because transform context is null." );
        }

        Object contextTarget = context.get( contextKey );
        if ( contextTarget == null ) {
            throw new TransformException( "Enrichr could not resolve contextKey '" + contextKey + "' at index:" + index + "." );
        }

        Method contextMethod = contextMethodCache.get( contextTarget.getClass() );
        if ( contextMethod == null ) {
            contextMethod = findMethod( contextTarget.getClass(), methodName, index );
            contextMethodCache.put( contextTarget.getClass(), contextMethod );
        }

        return new InvocationTarget( contextMethod, contextTarget );
    }

    /**
     * Convert supported return types into a completion stage.
     */
    @SuppressWarnings( "unchecked" )
    private static CompletionStage<Object> toCompletionStage( Object invocationResult ) {
        if ( invocationResult == null ) {
            return CompletableFuture.completedFuture( null );
        }
        if ( invocationResult instanceof CompletionStage ) {
            return (CompletionStage<Object>) invocationResult;
        }
        if ( invocationResult instanceof Publisher ) {
            return publisherToCompletionStage( (Publisher<?>) invocationResult );
        }
        return CompletableFuture.completedFuture( invocationResult );
    }

    /**
     * Bridge a single-value reactive publisher into a completion stage.
     */
    private static CompletionStage<Object> publisherToCompletionStage( Publisher<?> publisher ) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        publisher.subscribe( new Subscriber<Object>() {
            private Subscription subscription;
            private boolean hasValue;
            private Object value;

            @Override
            public void onSubscribe( Subscription subscription ) {
                this.subscription = subscription;
                subscription.request( Long.MAX_VALUE );
            }

            @Override
            public void onNext( Object nextValue ) {
                if ( hasValue ) {
                    subscription.cancel();
                    future.completeExceptionally( new TransformException( "Enrichr reactive enrichments must emit at most one value." ) );
                    return;
                }

                hasValue = true;
                value = nextValue;
            }

            @Override
            public void onError( Throwable throwable ) {
                future.completeExceptionally( throwable );
            }

            @Override
            public void onComplete() {
                future.complete( value );
            }
        } );
        return future;
    }

    /**
     * Find the first compatible public enrich method on the supplied class.
     */
    private static Method findMethod( Class<?> clazz, String methodName, int index ) {
        for ( Method candidate : clazz.getMethods() ) {
            if ( ! candidate.getName().equals( methodName ) ) {
                continue;
            }

            Class<?>[] parameterTypes = candidate.getParameterTypes();
            if ( parameterTypes.length < 1 || parameterTypes.length > 3 ) {
                continue;
            }
            if ( parameterTypes.length >= 2 && ! Object.class.isAssignableFrom( parameterTypes[1] ) ) {
                continue;
            }
            if ( parameterTypes.length == 3 && ! Map.class.isAssignableFrom( parameterTypes[2] ) ) {
                continue;
            }

            return candidate;
        }

        throw new SpecException(
                "Enrichr could not find a public method named '" + methodName + "' on " + clazz.getName() +
                        " with signature (Object), (Object,Object), or (Object,Object,Map) at index:" + index + "."
        );
    }

    /**
     * Concrete method plus object instance used for one invocation.
     */
    private static final class InvocationTarget {
        private final Method method;
        private final Object target;

        private InvocationTarget( Method method, Object target ) {
            this.method = method;
            this.target = target;
        }
    }
}
