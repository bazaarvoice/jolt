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

package com.bazaarvoice.jolt.templatr.function;

import com.bazaarvoice.jolt.common.Optional;

@SuppressWarnings( "unused" )
public class Math {

    public static final class MaxOf implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            Integer maxInt = Integer.MIN_VALUE;
            Double maxDouble = -(Double.MIN_VALUE);
            Long maxLong = Long.MIN_VALUE;

            if(args.length == 0) {
                return Optional.empty();
            }

            for(Object arg: args) {
                if(arg instanceof Integer) {
                    maxInt = java.lang.Math.max( maxInt, (Integer) arg );
                }
                else if(arg instanceof Double) {
                    maxDouble = java.lang.Math.max( maxDouble, (Double) arg );
                }
                else if(arg instanceof Long) {
                    maxLong = java.lang.Math.max(maxLong, (Long) arg);
                }
            }
            if(maxInt == Integer.MIN_VALUE && maxDouble == Double.MIN_VALUE && maxLong == Long.MIN_VALUE) {
                return Optional.empty();
            }
            // explicit if else to avoid autoboxing
            if(maxInt.longValue() >= maxDouble.longValue() && maxInt.longValue() >= maxLong) {
                return Optional.<Object>of(maxInt);
            }
            else if(maxLong >= maxDouble.longValue() && maxLong >= maxInt.longValue()) {
                return Optional.<Object>of(maxLong);
            }
            else {
                return Optional.<Object>of(maxDouble);
            }
        }
    }

    public static final class MinOf implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            Integer minInt = Integer.MAX_VALUE;
            Double minDouble = Double.MAX_VALUE;
            Long minLong = Long.MAX_VALUE;

            if(args.length == 0) {
                return Optional.empty();
            }

            for(Object arg: args) {
                if(arg instanceof Integer) {
                    minInt = java.lang.Math.min( minInt, (Integer) arg );
                }
                else if(arg instanceof Double) {
                    minDouble = java.lang.Math.min( minDouble, (Double) arg );
                }
                else if(arg instanceof Long) {
                    minLong = java.lang.Math.min( minLong, (Long) arg );
                }
            }
            if(minInt == Integer.MAX_VALUE && minDouble == Double.MAX_VALUE) {
                return Optional.empty();
            }
            // explicit if else to avoid autoboxing
            if(minInt.longValue() <= minDouble.longValue() && minInt.longValue() <= minLong) {
                return Optional.<Object>of(minInt);
            }
            else if(minLong <= minDouble.longValue() && minLong <= minInt.longValue()) {
                return Optional.<Object>of(minLong);
            }
            else {
                return Optional.<Object>of(minDouble);
            }
        }
    }

    public static final class Abs implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            Object arg = args[0];
            if(arg != null) {
                if(arg instanceof Integer) {
                    return Optional.<Object>of( java.lang.Math.abs( (Integer) arg ));
                }
                else if(arg instanceof Double) {
                    return Optional.<Object>of( java.lang.Math.abs( (Double) arg ));
                }
                else if(arg instanceof Long) {
                    return Optional.<Object>of( java.lang.Math.abs( (Long) arg ));
                }
            }
            return Optional.empty();
        }
    }

    public static final class toInteger implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            Object arg = args[0];
            if(arg != null && arg instanceof Number) {
                return Optional.<Object>of( ( (Number) arg ).intValue() );
            }
            else {
                return Optional.empty();
            }
        }
    }

    public static final class toLong implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            Object arg = args[0];
            if(arg != null && arg instanceof Number) {
                return Optional.<Object>of( ( (Number) arg ).longValue() );
            }
            else {
                return Optional.empty();
            }
        }
    }

    public static final class toDouble implements Function {
        @Override
        public Optional<Object> apply( final Object... args ) {
            if(args.length == 0) {
                return Optional.empty();
            }
            Object arg = args[0];
            if(arg != null && arg instanceof Number) {
                return Optional.<Object>of( ( (Number) arg ).doubleValue() );
            }
            else {
                return Optional.empty();
            }
        }
    }
}
