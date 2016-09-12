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

import java.util.List;

@SuppressWarnings( "deprecated" )
public class Math {

    /**
     * Given a list of objects, returns the max value in its appropriate type
     * also, interprets String as Number and returns appropriately
     *
     * max(1,2l,3d) == Optional.of(3d)
     * max(1,2l,"3.0") == Optional.of(3.0)
     * max("a", "b", "c") == Optional.empty()
     * max([]) == Optional.empty()
     */
    public static Optional<Number> max( List<Object> args ) {
        if(args == null || args.size() == 0) {
            return Optional.empty();
        }

        Integer maxInt = Integer.MIN_VALUE;
        Double maxDouble = -(Double.MAX_VALUE);
        Long maxLong = Long.MIN_VALUE;
        boolean found = false;

        for(Object arg: args) {
            if(arg instanceof Integer) {
                maxInt = java.lang.Math.max( maxInt, (Integer) arg );
                found = true;
            }
            else if(arg instanceof Double) {
                maxDouble = java.lang.Math.max( maxDouble, (Double) arg );
                found = true;
            }
            else if(arg instanceof Long) {
                maxLong = java.lang.Math.max(maxLong, (Long) arg);
                found = true;
            }
            else if(arg instanceof String) {
                Optional<?> optional = Objects.toNumber( arg );
                if(optional.isPresent()) {
                    arg = optional.get();
                    if(arg instanceof Integer) {
                        maxInt = java.lang.Math.max( maxInt, (Integer) arg );
                        found = true;
                    }
                    else if(arg instanceof Double) {
                        maxDouble = java.lang.Math.max( maxDouble, (Double) arg );
                        found = true;
                    }
                    else if(arg instanceof Long) {
                        maxLong = java.lang.Math.max(maxLong, (Long) arg);
                        found = true;
                    }
                }
            }
        }
        if(!found) {
            return Optional.empty();
        }

        // explicit getter method calls to avoid runtime autoboxing
        // autoBoxing will cause it to return the different type
        // check MathTest#testAutoBoxingIssue for example
        if(maxInt.longValue() >= maxDouble.longValue() && maxInt.longValue() >= maxLong) {
            return Optional.<Number>of(maxInt);
        }
        else if(maxLong >= maxDouble.longValue()) {
            return Optional.<Number>of(maxLong);
        }
        else {
            return Optional.<Number>of(maxDouble);
        }
    }

    /**
     * Given a list of objects, returns the min value in its appropriate type
     * also, interprets String as Number and returns appropriately
     *
     * min(1d,2l,3) == Optional.of(1d)
     * min("1.0",2l,d) == Optional.of(1.0)
     * min("a", "b", "c") == Optional.empty()
     * min([]) == Optional.empty()
     */
    public static Optional<Number> min( List<Object> args ) {
        if(args == null || args.size() == 0) {
            return Optional.empty();
        }
        Integer minInt = Integer.MAX_VALUE;
        Double minDouble = Double.MAX_VALUE;
        Long minLong = Long.MAX_VALUE;
        boolean found = false;

        for(Object arg: args) {
            if(arg instanceof Integer) {
                minInt = java.lang.Math.min( minInt, (Integer) arg );
                found = true;
            }
            else if(arg instanceof Double) {
                minDouble = java.lang.Math.min( minDouble, (Double) arg );
                found = true;
            }
            else if(arg instanceof Long) {
                minLong = java.lang.Math.min( minLong, (Long) arg );
                found = true;
            }
            else if(arg instanceof String) {
                Optional<?> optional = Objects.toNumber( arg );
                if(optional.isPresent()) {
                    arg = optional.get();
                    if(arg instanceof Integer) {
                        minInt = java.lang.Math.min( minInt, (Integer) arg );
                        found = true;
                    }
                    else if(arg instanceof Double) {
                        minDouble = java.lang.Math.min( minDouble, (Double) arg );
                        found = true;
                    }
                    else if(arg instanceof Long) {
                        minLong = java.lang.Math.min(minLong, (Long) arg);
                        found = true;
                    }
                }
            }
        }
        if(!found) {
            return Optional.empty();
        }
        // explicit getter method calls to avoid runtime autoboxing
        if(minInt.longValue() <= minDouble.longValue() && minInt.longValue() <= minLong) {
            return Optional.<Number>of(minInt);
        }
        else if(minLong <= minDouble.longValue()) {
            return Optional.<Number>of(minLong);
        }
        else {
            return Optional.<Number>of(minDouble);
        }
    }

    /**
     * Given any object, returns, if possible. its absolute value wrapped in Optional
     * Interprets String as Number
     *
     * abs("-123") == Optional.of(123)
     * abs("123") == Optional.of(123)
     * abs("12.3") == Optional.of(12.3)
     *
     * abs("abc") == Optional.empty()
     * abs(null) == Optional.empty()
     *
     */
    public static Optional<Number> abs( Object arg ) {
        if(arg instanceof Integer) {
            return Optional.<Number>of( java.lang.Math.abs( (Integer) arg ));
        }
        else if(arg instanceof Double) {
            return Optional.<Number>of( java.lang.Math.abs( (Double) arg ));
        }
        else if(arg instanceof Long) {
            return Optional.<Number>of( java.lang.Math.abs( (Long) arg ));
        }
        else if(arg instanceof String) {
            return abs( Objects.toNumber( arg ).get() );
        }
        return Optional.empty();
    }

    /**
     * Given a list of numbers, returns their avg as double
     * any value in the list that is not a valid number is ignored
     *
     * avg(2,"2","abc") == Optional.of(2.0)
     */
    public static Optional<Double> avg (List<Object> args) {
        double sum = 0d;
        int count = 0;
        for(Object arg: args) {
            Optional<? extends Number> numberOptional = Objects.toNumber( arg );
            if(numberOptional.isPresent()) {
                sum = sum + numberOptional.get().doubleValue();
                count = count + 1;
            }
        }
        return  count == 0 ? Optional.<Double>empty() : Optional.of( sum / count );
    }

    @SuppressWarnings( "unchecked" )
    public static final class max extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList( final List argList ) {
            return (Optional) max( argList );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static final class min extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList( final List<Object> argList ) {
            return (Optional) min( argList );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static final class abs extends Function.SingleFunction<Number> {
        @Override
        protected Optional<Number> applySingle( final Object arg ) {
            return abs( arg );
        }
    }

    @SuppressWarnings( "unchecked" )
    public static final class avg extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList( final List<Object> argList ) {
            return (Optional) avg( argList );
        }
    }
}
