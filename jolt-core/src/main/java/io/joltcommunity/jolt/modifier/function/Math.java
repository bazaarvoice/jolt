/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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

package io.joltcommunity.jolt.modifier.function;

import io.joltcommunity.jolt.common.Optional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.List;

@SuppressWarnings("deprecated")
public class Math {

    /**
     * Given a list of objects, returns the max value in its appropriate type
     * also, interprets String as Number and returns appropriately
     * <p>
     * max(1,2l,3d) == Optional.of(3d)
     * max(1,2l,"3.0") == Optional.of(3.0)
     * max("a", "b", "c") == Optional.empty()
     * max([]) == Optional.empty()
     */
    public static Optional<Number> max(List<Object> args) {
        if (args == null || args.size() == 0) {
            return Optional.empty();
        }

        Integer maxInt = Integer.MIN_VALUE;
        Double maxDouble = -(Double.MAX_VALUE);
        Long maxLong = Long.MIN_VALUE;
        boolean found = false;

        for (Object arg : args) {
            if (arg instanceof Integer) {
                maxInt = java.lang.Math.max(maxInt, (Integer) arg);
                found = true;
            } else if (arg instanceof Double) {
                maxDouble = java.lang.Math.max(maxDouble, (Double) arg);
                found = true;
            } else if (arg instanceof Long) {
                maxLong = java.lang.Math.max(maxLong, (Long) arg);
                found = true;
            } else if (arg instanceof String) {
                Optional<?> optional = Objects.toNumber(arg);
                if (optional.isPresent()) {
                    arg = optional.get();
                    if (arg instanceof Integer) {
                        maxInt = java.lang.Math.max(maxInt, (Integer) arg);
                        found = true;
                    } else if (arg instanceof Double) {
                        maxDouble = java.lang.Math.max(maxDouble, (Double) arg);
                        found = true;
                    } else if (arg instanceof Long) {
                        maxLong = java.lang.Math.max(maxLong, (Long) arg);
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            return Optional.empty();
        }

        // explicit getter method calls to avoid runtime autoboxing
        // autoBoxing will cause it to return the different type
        // check MathTest#testAutoBoxingIssue for example
        if (maxInt.longValue() >= maxDouble.longValue() && maxInt.longValue() >= maxLong) {
            return Optional.<Number>of(maxInt);
        } else if (maxLong >= maxDouble.longValue()) {
            return Optional.<Number>of(maxLong);
        } else {
            return Optional.<Number>of(maxDouble);
        }
    }

    /**
     * Given a list of objects, returns the min value in its appropriate type
     * also, interprets String as Number and returns appropriately
     * <p>
     * min(1d,2l,3) == Optional.of(1d)
     * min("1.0",2l,d) == Optional.of(1.0)
     * min("a", "b", "c") == Optional.empty()
     * min([]) == Optional.empty()
     */
    public static Optional<Number> min(List<Object> args) {
        if (args == null || args.size() == 0) {
            return Optional.empty();
        }
        Integer minInt = Integer.MAX_VALUE;
        Double minDouble = Double.MAX_VALUE;
        Long minLong = Long.MAX_VALUE;
        boolean found = false;

        for (Object arg : args) {
            if (arg instanceof Integer) {
                minInt = java.lang.Math.min(minInt, (Integer) arg);
                found = true;
            } else if (arg instanceof Double) {
                minDouble = java.lang.Math.min(minDouble, (Double) arg);
                found = true;
            } else if (arg instanceof Long) {
                minLong = java.lang.Math.min(minLong, (Long) arg);
                found = true;
            } else if (arg instanceof String) {
                Optional<?> optional = Objects.toNumber(arg);
                if (optional.isPresent()) {
                    arg = optional.get();
                    if (arg instanceof Integer) {
                        minInt = java.lang.Math.min(minInt, (Integer) arg);
                        found = true;
                    } else if (arg instanceof Double) {
                        minDouble = java.lang.Math.min(minDouble, (Double) arg);
                        found = true;
                    } else if (arg instanceof Long) {
                        minLong = java.lang.Math.min(minLong, (Long) arg);
                        found = true;
                    }
                }
            }
        }
        if (!found) {
            return Optional.empty();
        }
        // explicit getter method calls to avoid runtime autoboxing
        if (minInt.longValue() <= minDouble.longValue() && minInt.longValue() <= minLong) {
            return Optional.<Number>of(minInt);
        } else if (minLong <= minDouble.longValue()) {
            return Optional.<Number>of(minLong);
        } else {
            return Optional.<Number>of(minDouble);
        }
    }

    /**
     * Given any object, returns, if possible. its absolute value wrapped in Optional
     * Interprets String as Number
     * <p>
     * abs("-123") == Optional.of(123)
     * abs("123") == Optional.of(123)
     * abs("12.3") == Optional.of(12.3)
     * <p>
     * abs("abc") == Optional.empty()
     * abs(null) == Optional.empty()
     */
    public static Optional<Number> abs(Object arg) {
        if (arg instanceof Integer) {
            return Optional.<Number>of(java.lang.Math.abs((Integer) arg));
        } else if (arg instanceof Double) {
            return Optional.<Number>of(java.lang.Math.abs((Double) arg));
        } else if (arg instanceof Long) {
            return Optional.<Number>of(java.lang.Math.abs((Long) arg));
        } else if (arg instanceof String) {
            return abs(Objects.toNumber(arg).get());
        }
        return Optional.empty();
    }

    /**
     * Given a list of numbers, returns their avg as double
     * any value in the list that is not a valid number is ignored
     * <p>
     * avg(2,"2","abc") == Optional.of(2.0)
     */
    public static Optional<Double> avg(List<Object> args) {
        double sum = 0d;
        int count = 0;
        for (Object arg : args) {
            Optional<? extends Number> numberOptional = Objects.toNumber(arg);
            if (numberOptional.isPresent()) {
                sum = sum + numberOptional.get().doubleValue();
                count = count + 1;
            }
        }
        return count == 0 ? Optional.<Double>empty() : Optional.of(sum / count);
    }

    public static Optional<Integer> intSum(List<Object> args) {
        Integer sum = 0;
        for (Object arg : args) {
            Optional<? extends Integer> numberOptional = Objects.toInteger(arg);
            if (numberOptional.isPresent()) {
                sum = sum + numberOptional.get();
            }
        }
        return Optional.of(sum);
    }

    public static Optional<Double> doubleSum(List<Object> args) {
        Double sum = 0.0;
        for (Object arg : args) {
            Optional<? extends Double> numberOptional = Objects.toDouble(arg);
            if (numberOptional.isPresent()) {
                sum = sum + numberOptional.get();
            }
        }
        return Optional.of(sum);
    }

    public static Optional<Long> longSum(List<Object> args) {
        Long sum = 0l;
        for (Object arg : args) {
            Optional<? extends Long> numberOptional = Objects.toLong(arg);
            if (numberOptional.isPresent()) {
                sum = sum + numberOptional.get();
            }
        }
        return Optional.of(sum);
    }

    public static Optional<Integer> intSubtract(List<Object> argList) {

        if (argList == null || argList.size() != 2) {
            return Optional.empty();
        }

        if (!(argList.get(0) instanceof Integer && argList.get(1) instanceof Integer)) {
            return Optional.empty();
        }

        int a = (Integer) argList.get(0);
        int b = (Integer) argList.get(1);

        return Optional.of(a - b);
    }

    public static Optional<Double> doubleSubtract(List<Object> argList) {

        if (argList == null || argList.size() != 2) {
            return Optional.empty();
        }

        if (!(argList.get(0) instanceof Double && argList.get(1) instanceof Double)) {
            return Optional.empty();
        }

        double a = (Double) argList.get(0);
        double b = (Double) argList.get(1);

        return Optional.of(a - b);
    }

    public static Optional<Long> longSubtract(List<Object> argList) {

        if (argList == null || argList.size() != 2) {
            return Optional.empty();
        }

        if (!(argList.get(0) instanceof Long && argList.get(1) instanceof Long)) {
            return Optional.empty();
        }

        long a = (Long) argList.get(0);
        long b = (Long) argList.get(1);

        return Optional.of(a - b);
    }

    public static Optional<? extends Number> multiply(List<Object> argList) {
        if (argList.size() != 2) {
            return Optional.empty();
        }

        Optional<? extends Number> leftOpt = Objects.toNumber(argList.get(0));
        Optional<? extends Number> rightOpt = Objects.toNumber(argList.get(1));

        if (!leftOpt.isPresent() || !rightOpt.isPresent()) {
            return Optional.empty();
        }

        Number left = leftOpt.get();
        Number right = rightOpt.get();

        // 1. If either is BigDecimal
        if (left instanceof BigDecimal || right instanceof BigDecimal) {
            BigDecimal l = (left instanceof BigDecimal) ? (BigDecimal) left : new BigDecimal(left.toString());
            BigDecimal r = (right instanceof BigDecimal) ? (BigDecimal) right : new BigDecimal(right.toString());
            return Optional.of(l.multiply(r));
        }
        // 2. If left is BigInteger and right is Double
        if (left instanceof BigInteger && right instanceof Double) {
            BigDecimal l = new BigDecimal(left.toString());
            BigDecimal r = BigDecimal.valueOf((Double) right);
            return Optional.of(l.multiply(r));
        }
        // 3. If left is Double and right is BigInteger
        if (left instanceof Double && right instanceof BigInteger) {
            BigDecimal l = BigDecimal.valueOf((Double) left);
            BigDecimal r = new BigDecimal(right.toString());
            return Optional.of(l.multiply(r));
        }
        // 4. If either is BigInteger
        if (left instanceof BigInteger || right instanceof BigInteger) {
            BigInteger l = (left instanceof BigInteger) ? (BigInteger) left : BigInteger.valueOf(left.longValue());
            BigInteger r = (right instanceof BigInteger) ? (BigInteger) right : BigInteger.valueOf(right.longValue());
            return Optional.of(l.multiply(r));
        }
        // 5. If either is Double
        if (left instanceof Double || right instanceof Double) {
            return Optional.of(left.doubleValue() * right.doubleValue());
        }
        // 6. Otherwise, use long multiplication
        return Optional.of(left.longValue() * right.longValue());
    }

    public static Optional<? extends Number> multiplyAndRound(List<Object> argList, int digitsAfterDecimalPoint, RoundingMode roundingMode) {
        Optional<? extends Number> result = multiply(argList);
        if (result.isPresent()) {
            Number resultValue = result.get();
            if (resultValue instanceof Double) {
                BigDecimal bigDecimal = BigDecimal.valueOf((Double) resultValue).setScale(digitsAfterDecimalPoint, roundingMode);
                return Optional.of(bigDecimal.doubleValue());
            }
        }
        return result;
    }

    public static Optional<Double> divide(List<Object> argList) {

        if (argList == null || argList.size() != 2) {
            return Optional.empty();
        }

        Optional<? extends Number> numerator = Objects.toNumber(argList.get(0));
        Optional<? extends Number> denominator = Objects.toNumber(argList.get(1));

        if (numerator.isPresent() && denominator.isPresent()) {

            Double drDoubleValue = denominator.get().doubleValue();
            if (drDoubleValue == 0) {
                return Optional.empty();
            }

            Double nrDoubleValue = numerator.get().doubleValue();
            Double result = nrDoubleValue / drDoubleValue;
            return Optional.of(result);
        }

        return Optional.empty();
    }

    public static Optional<Double> divideAndRound(List<Object> argList, int digitsAfterDecimalPoint, RoundingMode roundingMode) {

        Optional<Double> divideResult = divide(argList);

        if (divideResult.isPresent()) {
            Double divResult = divideResult.get();
            BigDecimal bigDecimal = new BigDecimal(divResult).setScale(digitsAfterDecimalPoint, roundingMode);
            return Optional.of(bigDecimal.doubleValue());
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static final class max extends Function.BaseFunction<Object> {
        @Override
        protected Optional<Object> applyList(final List argList) {
            return (Optional) max(argList);
        }

        @Override
        protected Optional<Object> applySingle(final Object arg) {
            if (arg instanceof Number) {
                return Optional.of(arg);
            } else {
                return Optional.empty();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static final class min extends Function.BaseFunction<Object> {

        @Override
        protected Optional<Object> applyList(final List<Object> argList) {
            return (Optional) min(argList);
        }

        @Override
        protected Optional<Object> applySingle(Object arg) {
            if (arg instanceof Number) {
                return Optional.of(arg);
            } else {
                return Optional.empty();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static final class abs extends Function.SingleFunction<Number> {
        @Override
        protected Optional<Number> applySingle(final Object arg) {
            return abs(arg);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class multiply extends Function.ListFunction {

        @Override
        protected Optional<Object> applyList(List<Object> argList) {
            return (Optional) multiply(argList);
        }

    }

    @SuppressWarnings("unchecked")
    public static final class multiplyAndRound extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(List<Object> argList) {
            do {
                if (argList.size() < 3) {
                    break;
                }
                Object digitsObj = argList.get(0);

                if (!(digitsObj instanceof Integer)) {
                    break;
                }
                int digitsAfterDecimalPoint = (Integer) digitsObj;
                if (argList.size() == 3) {
                    List<Object> numbers = argList.subList(1, argList.size());
                    return (Optional) multiplyAndRound(numbers, digitsAfterDecimalPoint, RoundingMode.HALF_UP);
                }
                Object roundingModeObj = argList.get(1);
                if (!(roundingModeObj instanceof String)) {
                    break;
                }
                RoundingMode roundingMode;
                try {
                    roundingMode = RoundingMode.valueOf(((String) roundingModeObj).toUpperCase());
                } catch (IllegalArgumentException e) {
                    break;
                }
                List<Object> numbers = argList.subList(2, argList.size());
                return (Optional) multiplyAndRound(numbers, digitsAfterDecimalPoint, roundingMode);
            } while (false);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static final class divide extends Function.ListFunction {

        @Override
        protected Optional<Object> applyList(List<Object> argList) {
            return (Optional) divide(argList);
        }

    }

    @SuppressWarnings("unchecked")
    public static final class divideAndRound extends Function.ListFunction {

        @Override
        protected Optional<Object> applyList(List<Object> argList) {
            do {
                if (argList.size() < 3) {
                    break;
                }
                Object digitsObj = argList.get(0);

                if (!(digitsObj instanceof Integer)) {
                    break;
                }
                int digitsAfterDecimalPoint = (Integer) digitsObj;
                if (argList.size() == 3) {
                    List<Object> numbers = argList.subList(1, argList.size());
                    return (Optional) divideAndRound(numbers, digitsAfterDecimalPoint, RoundingMode.HALF_UP);
                }
                Object roundingModeObj = argList.get(1);
                if (!(roundingModeObj instanceof String)) {
                    break;
                }
                RoundingMode roundingMode;
                try {
                    roundingMode = RoundingMode.valueOf(((String) roundingModeObj).toUpperCase());
                } catch (IllegalArgumentException e) {
                    break;
                }
                List<Object> numbers = argList.subList(2, argList.size());
                return (Optional) divideAndRound(numbers, digitsAfterDecimalPoint, roundingMode);
            } while (false);
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    public static final class avg extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argList) {
            return (Optional) avg(argList);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class intSum extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argIntList) {
            return (Optional) intSum(argIntList);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class doubleSum extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argDoubleList) {
            return (Optional) doubleSum(argDoubleList);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class longSum extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argLongList) {
            return (Optional) longSum(argLongList);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class intSubtract extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argIntList) {
            return (Optional) intSubtract(argIntList);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class doubleSubtract extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argDoubleList) {
            return (Optional) doubleSubtract(argDoubleList);
        }
    }

    @SuppressWarnings("unchecked")
    public static final class longSubtract extends Function.ListFunction {
        @Override
        protected Optional<Object> applyList(final List<Object> argLongList) {
            return (Optional) longSubtract(argLongList);
        }
    }
}
