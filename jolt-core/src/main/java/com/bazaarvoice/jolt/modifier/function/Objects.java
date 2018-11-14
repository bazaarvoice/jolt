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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Objects {

    /**
     * Given any object, returns, if possible. its Java number equivalent wrapped in Optional
     * Interprets String as Number
     *
     * toNumber("123") == Optional.of(123)
     * toNumber("-123") == Optional.of(-123)
     * toNumber("12.3") == Optional.of(12.3)
     *
     * toNumber("abc") == Optional.empty()
     * toNumber(null) == Optional.empty()
     *
     * also, see: MathTest#testNitPicks
     *
     */
    public static Optional<? extends Number> toNumber(Object arg) {
        if ( arg instanceof Number ) {
            return Optional.of( ( (Number) arg ));
        }
        else if(arg instanceof String) {
            try {
                return Optional.of( (Number) Integer.parseInt( (String) arg ) );
            }
            catch(Exception ignored) {}
            try {
                return Optional.of( (Number) Long.parseLong( (String) arg ) );
            }
            catch(Exception ignored) {}
            try {
                return Optional.of( (Number) Double.parseDouble( (String) arg ) );
            }
            catch(Exception ignored) {}
            return Optional.empty();
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns int value of argument, if possible, wrapped in Optional
     * Interprets String as Number
     */
    public static Optional<Integer> toInteger(Object arg) {
        if ( arg instanceof Number ) {
            return Optional.of( ( (Number) arg ).intValue() );
        }
        else if(arg instanceof String) {
            Optional<? extends Number> optional = toNumber( arg );
            if ( optional.isPresent() ) {
                return Optional.of( optional.get().intValue() );
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns long value of argument, if possible, wrapped in Optional
     * Interprets String as Number
     */
    public static Optional<Long> toLong(Object arg) {
        if ( arg instanceof Number ) {
            return Optional.of( ( (Number) arg ).longValue() );
        }
        else if(arg instanceof String) {
            Optional<? extends Number> optional = toNumber( arg );
            if ( optional.isPresent() ) {
                return Optional.of( optional.get().longValue() );
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns double value of argument, if possible, wrapped in Optional
     * Interprets String as Number
     */
    public static Optional<Double> toDouble(Object arg) {
        if ( arg instanceof Number ) {
            return Optional.of( ( (Number) arg ).doubleValue() );
        }
        else if(arg instanceof String) {
            Optional<? extends Number> optional = toNumber( arg );
            if ( optional.isPresent() ) {
                return Optional.of( optional.get().doubleValue() );
            }
            else {
                return Optional.empty();
            }
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Returns boolean value of argument, if possible, wrapped in Optional
     * Interprets Strings "true" & "false" as boolean
     */
    public static Optional<Boolean> toBoolean(Object arg) {
        if ( arg instanceof Boolean ) {
            return Optional.of( (Boolean) arg );
        }
        else if(arg instanceof String) {
            if("true".equalsIgnoreCase( (String)arg )) {
                return Optional.of( Boolean.TRUE );
            }
            else if("false".equalsIgnoreCase( (String)arg )) {
                return Optional.of( Boolean.FALSE );
            }
        }
        return Optional.empty();
    }

    /**
     * Returns String representation of argument, wrapped in Optional
     *
     * for array argument, returns Arrays.toString()
     * for others, returns Objects.toString()
     *
     * Note: this method does not return Optional.empty()
     */
    public static Optional<String> toString(Object arg) {
        if ( arg instanceof String ) {
            return Optional.of( (String) arg );
        }
        else if ( arg instanceof Object[] ) {
            return Optional.of( Arrays.toString( (Object[] )arg ) );
        }
        else {
            return Optional.of( java.util.Objects.toString( arg ) );
        }
    }

    /**
     * Squashes nulls in a list or map.
     *
     * Modifies the data.
     */
    public static void squashNulls( Object input ) {
        if ( input instanceof List ) {
            List inputList = (List) input;
            inputList.removeIf( java.util.Objects::isNull );
        }
        else if ( input instanceof Map ) {
            Map<String,Object> inputMap = (Map<String,Object>) input;

            List<String> keysToNuke = new ArrayList<>();
            for (Map.Entry<String,Object> entry : inputMap.entrySet()) {
                if ( entry.getValue() == null ) {
                    keysToNuke.add( entry.getKey() );
                }
            }

            inputMap.keySet().removeAll( keysToNuke );
        }
    }

    /**
     * Recursively squash nulls in maps and lists.
     *
     * Modifies the data.
     */
    public static void recursivelySquashNulls(Object input) {

        // Makes two passes thru the data.
        Objects.squashNulls( input );

        if ( input instanceof List ) {
            List inputList = (List) input;
            inputList.forEach( i -> recursivelySquashNulls( i ) );
        }
        else if ( input instanceof Map ) {
            Map<String,Object> inputMap = (Map<String,Object>) input;

            for (Map.Entry<String,Object> entry : inputMap.entrySet()) {
                recursivelySquashNulls( entry.getValue() );
            }
        }
    }



    public static final class toInteger extends Function.SingleFunction<Integer> {
        @Override
        protected Optional<Integer> applySingle( final Object arg ) {
            return toInteger( arg );
        }
    }

    public static final class toLong extends Function.SingleFunction<Long> {
        @Override
        protected Optional<Long> applySingle( final Object arg ) {
            return toLong( arg );
        }
    }

    public static final class toDouble extends Function.SingleFunction<Double> {
        @Override
        protected Optional<Double> applySingle( final Object arg ) {
            return toDouble( arg );
        }
    }

    public static final class toBoolean extends Function.SingleFunction<Boolean> {
        @Override
        protected Optional<Boolean> applySingle( final Object arg ) {
            return toBoolean( arg );
        }
    }

    public static final class toString extends Function.SingleFunction<String> {
        @Override
        protected Optional<String> applySingle( final Object arg ) {
            return Objects.toString( arg );
        }
    }

    public static final class squashNulls extends Function.SingleFunction<Object> {
        @Override
        protected Optional<Object> applySingle( final Object arg ) {
            Objects.squashNulls( arg );
            return Optional.of( arg );
        }
    }

    public static final class recursivelySquashNulls extends Function.SingleFunction<Object> {
        @Override
        protected Optional<Object> applySingle( final Object arg ) {
            Objects.recursivelySquashNulls( arg );
            return Optional.of( arg );
        }
    }

    /**
     * Size is a special snowflake and needs specific care
     */
    public static final class size implements Function {

        @Override
        public Optional<Object> apply(Object... args) {
            if(args.length == 0) {
                return Optional.empty();
            }
            else if(args.length == 1) {
                if(args[0] == null) {
                    return Optional.empty();
                }
                else if(args[0] instanceof List ) {
                    return Optional.of(((List) args[0]).size());
                }
                else if(args[0] instanceof String) {
                    return Optional.of( ((String) args[0]).length() );
                }
                else if(args[0] instanceof Map) {
                    return Optional.of( ((Map) args[0]).size() );
                }
                else {
                    return Optional.empty();
                }
            }
            else {
                return Optional.of(args.length);
            }
        }
    }
}
