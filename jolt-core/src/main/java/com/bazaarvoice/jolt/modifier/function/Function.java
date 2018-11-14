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

package com.bazaarvoice.jolt.modifier.function;

import com.bazaarvoice.jolt.common.Optional;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Modifier supports a Function on RHS that accepts jolt path expressions as arguments and evaluates
 * them at runtime before calling it. Function always returns an Optional, and the value is written
 * only if the optional is not empty.
 *
 * function spec is defined by "key": "=functionName(args...)"
 *
 *
 * input:
 *      { "num": -1.0 }
 * spec:
 *      { "num": "=abs(@(1,&0))" }
 *      will call the stock function Math.abs() and will pass the matching value at "num"
 *
 * spec:
 *      { "num": "=abs" }
 *      an alternative shortcut will do the same thing
 *
 * output:
 *      { "num": 1.0 }
 *
 *
 *
 * input:
 *      { "value": -1.0 }
 *
 * spec:
 *      { "absValue": "=abs(@(1,value))" }
 *      will evaluate the jolt path expression @(1,value) and pass the output to stock function Math.abs()
 *
 * output:
 *      { "value": -1.0, "absValue": 1.0 }
 *
 *
 *
 * Currently defined stock functions are:
 *
 *      toLower     - returns toLower value of toString() value of first arg, rest is ignored
 *      toUpper     - returns toUpper value of toString() value of first arg, rest is ignored
 *      concat      - concatenate all given arguments' toString() values
 *
 *      min       - returns the min of all numbers provided in the arguments, non-numbers are ignored
 *      max       - returns the max of all numbers provided in the arguments, non-numbers are ignored
 *      abs         - returns the absolute value of first argument, rest is ignored
 *      toInteger   - returns the intValue() value of first argument if its numeric, rest is ignored
 *      toDouble    - returns the doubleValue() value of first argument if its numeric, rest is ignored
 *      toLong      - returns the longValue() value of first argument if its numeric, rest is ignored
 *
 * All of these functions returns Optional.EMPTY if unsuccessful, which results in a no-op when performing
 * the actual write in the json doc.
 *
 * i.e.
 * input:
 *      { "value1": "xyz" } --- note: string, not number
 *      { "value1": "1.0" } --- note: string, not number
 *
 * spec:
 *      { "value1": "=abs" } --- fails silently
 *      { "value2": "=abs" }
 *
 * output:
 *      { "value1": "xyz", "value2": "1" } --- note: "absValue": null is not inserted
 *
 *
 * This is work in progress, and probably will be changed in future releases. Hence it is marked for
 * removal as it'll eventually be moved to a different package as the Function feature is baked into
 * other transforms as well. In short this interface is not yet ready to be implemented outside jolt!
 *
 */

@Deprecated
public interface Function {

    Optional<Object> apply(Object... args);

    /**
     * Does nothing
     *
     * spec - "key": "=noop"
     *
     * will cause the key to remain unchanged
     */
    Function noop = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            return Optional.empty();
        }
    };

    /**
     * Returns the first argument, null or otherwise
     *
     * spec - "key": [ "=isPresent", "otherValue" ]
     *
     * input - "key": null
     * output - "key": null
     *
     * input - "key": "value"
     * output - "key": "value"
     *
     * input - key is missing
     * output - "key": "otherValue"
     *
     */
    Function isPresent = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if (args.length == 0) {
                return Optional.empty();
            }
            return Optional.of( args[0] );
        }
    };

    /**
     * Returns the first argument if in not null
     *
     * spec - "key": ["=notNull", "otherValue" ]
     *
     * input - "key": null
     * output - "key": "otherValue"
     *
     * input - "key": "value"
     * output - "key": "value"
     *
     */
    Function notNull = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if (args.length == 0 || args[0] == null) {
                return Optional.empty();
            }
            return Optional.of( args[0] );
        }
    };

    /**
     * Returns the first argument if it is null
     *
     * spec - "key": ["=inNull", "otherValue" ]
     *
     * input - "key": null
     * output - "key": null
     *
     * input - "key": "value"
     * output - "key": "otherValue"
     *
     */
    Function isNull = new Function() {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if (args.length == 0 || args[0] != null) {
                return Optional.empty();
            }
            return Optional.of( args[0] );
        }
    };

    /**
     * Abstract class that processes var-args and calls two abstract methods
     *
     * If its single list arg, or many args, calls applyList()
     * else calls applySingle()
     *
     * @param <T> type of return value
     */
    @SuppressWarnings( "unchecked" )
    abstract class BaseFunction<T> implements Function {

        public final Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            else if(args.length == 1) {
                if(args[0] instanceof List ) {
                    if(((List) args[0]).isEmpty()) {
                        return Optional.empty();
                    }
                    else {
                        return applyList((List) args[0]);
                    }
                }
                else if( args[0] instanceof Object[] ) {
                    if(((Object[]) args[0]).length == 0) {
                        return Optional.empty();
                    }
                    else {
                        return applyList(Arrays.asList(((Object[]) args[0])));
                    }
                }
                else if(args[0] == null) {
                    return Optional.empty();
                }
                else {
                    return (Optional) applySingle( args[0] );
                }
            }
            else {
                return applyList( Arrays.asList( args ) );
            }
        }

        protected abstract Optional<Object> applyList( final List<Object> input );

        protected abstract Optional<T> applySingle( final Object arg );
    }

    /**
     * Abstract class that provides rudimentary abstraction to quickly implement
     * a function that works on an single value input
     *
     * i.e. toUpperCase a string
     *
     * @param <T> type of return value
     */
    @SuppressWarnings( "unchecked" )
    abstract class SingleFunction<T> extends BaseFunction<T> {

        protected final Optional<Object> applyList( final List<Object> input ) {
            List<Object> ret = new ArrayList<>( input.size() );
            for(Object o: input) {
                Optional<T> optional = applySingle( o );
                ret.add(optional.isPresent()?optional.get():o);
            }
            return Optional.<Object>of( ret );
        }

        protected abstract Optional<T> applySingle( final Object arg );
    }

    /**
     * Abstract class that provides rudimentary abstraction to quickly implement
     * a function that works on an List of input
     *
     * i.e. find the max item from a list, etc.
     *
     */
    @SuppressWarnings( "unchecked" )
    abstract class ListFunction extends BaseFunction<Object> {

        protected abstract Optional<Object> applyList( final List<Object> argList );

        protected final Optional<Object> applySingle( final Object arg ) {
            return Optional.empty();
        }
    }

    /**
     * Abstract class that provides rudimentary abstraction to quickly implement
     * a function that classifies first arg as special input and rest as regular
     * input.
     *
     * @param <SOURCE> type of special argument
     * @param <RETTYPE> type of return value
     */
    @SuppressWarnings( "unchecked" )
    abstract class ArgDrivenFunction<SOURCE, RETTYPE> implements Function {

        private final Class<SOURCE> specialArgType;

        private ArgDrivenFunction() {
            /**
             * inspired from {@link com.google.common.reflect.TypeCapture#capture()}
             * copied, coz jolt-core is designed to have no dependency
             * modified, coz the instanceof check and subsequently throwing exception
             * is unnecessary as we already know this class has genericSuperClass of
             * Parametrized type. In worst case if an implementation does not specify
             * the generics, we fall back to Object.class, and that's ok.
             */
            Type superclass = getClass().getGenericSuperclass();
            if(superclass instanceof ParameterizedType) {
                specialArgType =  (Class<SOURCE>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
            }
            else {
                specialArgType =  (Class<SOURCE>) Object.class;
            }
        }

        private Optional<SOURCE> getSpecialArg( Object[] args) {
            if ( (args.length >= 2) && specialArgType.isInstance( args[0]) ) {
                SOURCE specialArg = (SOURCE) args[0];
                return Optional.of( specialArg );
            }
            return Optional.empty();
        }

        @Override
        public final Optional<Object> apply( Object... args ) {

            if(args.length == 1 && args[0] instanceof List) {
                args = ((List) args[0]).toArray();
            }

            Optional<SOURCE> specialArgOptional = getSpecialArg( args );
            if ( specialArgOptional.isPresent() ) {
                SOURCE specialArg = specialArgOptional.get();
                if ( args.length == 2) {
                    if(args[1] instanceof List) {
                        return (Optional) applyList( specialArg, (List) args[1] );
                    }
                    else {
                        return (Optional) applySingle( specialArg, args[1] );
                    }
                }
                else {
                    List<Object> input = Arrays.asList( Arrays.copyOfRange(args, 1, args.length) );
                    return applyList( specialArg, input );
                }
            }
            else {
                return Optional.empty();
            }
        }

        protected abstract Optional<Object> applyList( SOURCE specialArg, List<Object> args );

        protected abstract Optional<RETTYPE> applySingle( SOURCE specialArg, Object arg );
    }

    /**
     * Extends ArgDrivenConverter to provide rudimentary abstraction to quickly
     * implement a function that works on a single input
     *
     * i.e. increment(1, value)
     *
     * @param <S> type of special argument
     * @param <R> type of return value
     */
    @SuppressWarnings( "unchecked" )
    abstract class ArgDrivenSingleFunction<S, R> extends ArgDrivenFunction<S, R> {

        protected final Optional<Object> applyList( S specialArg, List<Object> input ) {
            List<Object> ret = new ArrayList<>( input.size() );
            for(Object o: input) {
                Optional<R> optional = applySingle( specialArg, o );
                ret.add(optional.isPresent()?optional.get():o);
            }
            return (Optional) Optional.of( ret );
        }

        protected abstract Optional<R> applySingle( S specialArg, Object arg );
    }

    /**
     * Extends ArgDrivenConverter to provide rudimentary abstraction to quickly
     * implement a function that works on an input list|array
     *
     * i.e. join('-', ...)
     *
     * @param <S> type of special argument
     */
    @SuppressWarnings( "unchecked" )
    abstract class ArgDrivenListFunction<S> extends ArgDrivenFunction<S, Object> {

        protected abstract Optional<Object> applyList( S specialArg, List<Object> args );

        protected final Optional<Object> applySingle( S specialArg, Object arg ) {
            return Optional.empty();
        }
    }

}
