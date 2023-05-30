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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.bazaarvoice.jolt.modifier.function.Math.abs;
import static com.bazaarvoice.jolt.modifier.function.Objects.toNumber;

@SuppressWarnings( "deprecated" )
public class MathTest extends AbstractTester {

    @DataProvider(parallel = true)
    public Iterator<Object[]> getTestCases() {
        List<Object[]> testCases = new LinkedList<>(  );

        Function MAX_OF = new Math.max();
        Function MIN_OF = new Math.min();
        Function ABS_OF = new Math.abs();
        Function TO_INTEGER = new Objects.toInteger();
        Function TO_DOUBLE = new Objects.toDouble();
        Function TO_LONG = new Objects.toLong();

        Function INT_SUM_OF = new Math.intSum();
        Function DOUBLE_SUM_OF = new Math.doubleSum();
        Function LONG_SUM_OF = new Math.longSum();

        Function INT_SUBTRACT_OF = new Math.intSubtract();
        Function DOUBLE_SUBTRACT_OF = new Math.doubleSubtract();
        Function LONG_SUBTRACT_OF = new Math.longSubtract();

        Function DIV_OF = new Math.divide();
        Function DIV_AND_ROUND_OF = new Math.divideAndRound();
        
        Function MULTIPLY_OF = new Math.multiply();

        testCases.add( new Object[] { "max-empty-array", MAX_OF, new Object[] {}, Optional.empty() } );
        testCases.add( new Object[] { "max-empty-list", MAX_OF, new ArrayList( ), Optional.empty() } );
        testCases.add( new Object[] { "max-null", MAX_OF, null, Optional.empty() } );
        testCases.add( new Object[] { "max-object", MAX_OF, new Object(), Optional.empty() } );

        testCases.add( new Object[] { "max-single-int-array", MAX_OF, new Object[] {1}, Optional.of( 1 ) } );
        testCases.add( new Object[] { "max-single-long-array", MAX_OF, new Object[] {1L}, Optional.of( 1L ) } );
        testCases.add( new Object[] { "max-single-double-array", MAX_OF, new Object[] {1.0}, Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "max-single-int-list", MAX_OF, Arrays.asList( 1 ), Optional.of( 1 ) } );
        testCases.add( new Object[] { "max-single-long-list", MAX_OF, Arrays.asList( 1L ), Optional.of( 1L ) } );
        testCases.add( new Object[] { "max-single-double-list", MAX_OF, Arrays.asList( 1.0 ), Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "max-single-int-array-extra-arg", MAX_OF, new Object[] {1, "a"}, Optional.of( 1 ) } );
        testCases.add( new Object[] { "max-single-long-array-extra-arg", MAX_OF, new Object[] {1L, "a"}, Optional.of( 1L ) } );
        testCases.add( new Object[] { "max-single-double-array-extra-arg", MAX_OF, new Object[] {1.0, "a"}, Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "max-single-int-list-extra-arg", MAX_OF, Arrays.asList( 1, "a" ), Optional.of( 1 ) } );
        testCases.add( new Object[] { "max-single-long-list-extra-arg", MAX_OF, Arrays.asList( 1L, "a" ), Optional.of( 1L ) } );
        testCases.add( new Object[] { "max-single-double-list-extra-arg", MAX_OF, Arrays.asList( 1.0, "a" ), Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "max-multi-int-array", MAX_OF, new Object[] {1, 3, 2, 5}, Optional.of( 5 ) } );
        testCases.add( new Object[] { "max-multi-long-array", MAX_OF, new Object[] {1L, 3L, 2L, 5L}, Optional.of( 5L ) } );
        testCases.add( new Object[] { "max-multi-double-array", MAX_OF, new Object[] {1.0, 3.0, 2.0, 5.0}, Optional.of( 5.0 ) } );

        testCases.add( new Object[] { "max-multi-int-list", MAX_OF, Arrays.asList( 1, 3, 2, 5 ), Optional.of( 5 ) } );
        testCases.add( new Object[] { "max-multi-long-list", MAX_OF, Arrays.asList( 1L, 3L, 2L, 5L ), Optional.of( 5L ) } );
        testCases.add( new Object[] { "max-multi-double-list", MAX_OF, Arrays.asList( 1.0, 3.0, 2.0, 5.0 ), Optional.of( 5.0 ) } );

        testCases.add( new Object[] { "max-combo-int-array", MAX_OF, new Object[] {1.0, 3L, null, 5}, Optional.of( 5 ) } );
        testCases.add( new Object[] { "max-combo-long-array", MAX_OF, new Object[] {1.0, 3L, null, 5L}, Optional.of( 5L ) } );
        testCases.add( new Object[] { "max-combo-double-array", MAX_OF, new Object[] {1.0, 3L, null, 5.0}, Optional.of( 5.0 ) } );

        testCases.add( new Object[] { "max-combo-int-list", MAX_OF, Arrays.asList( 1.0, 3L, null, 5 ), Optional.of( 5 ) } );
        testCases.add( new Object[] { "max-combo-long-list", MAX_OF, Arrays.asList( 1.0, 3L, null, 5L ), Optional.of( 5L ) } );
        testCases.add( new Object[] { "max-combo-double-list", MAX_OF, Arrays.asList( 1.0, 3L, null, 5.0 ), Optional.of( 5.0 ) } );

        testCases.add( new Object[] { "max-NaN", MAX_OF, Arrays.asList( 1.0, Double.NaN ), Optional.of( Double.NaN ) } );
        testCases.add( new Object[] { "max-positive-infinity", MAX_OF, Arrays.asList( 1.0, Double.POSITIVE_INFINITY ), Optional.of( Double.POSITIVE_INFINITY ) } );
        testCases.add( new Object[] { "max-NaN-positive-infinity", MAX_OF, Arrays.asList( 1.0, Double.NaN, Double.POSITIVE_INFINITY ), Optional.of( Double.NaN ) } );



        testCases.add( new Object[] { "min-empty-array", MIN_OF, new Object[] {}, Optional.empty() } );
        testCases.add( new Object[] { "min-empty-list", MIN_OF, new ArrayList( ), Optional.empty() } );
        testCases.add( new Object[] { "min-null", MIN_OF, null, Optional.empty() } );
        testCases.add( new Object[] { "min-object", MIN_OF, new Object(), Optional.empty() } );

        testCases.add( new Object[] { "min-single-int-array", MIN_OF, new Object[] {1}, Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-single-long-array", MIN_OF, new Object[] {1L}, Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-single-double-array", MIN_OF, new Object[] {1.0}, Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-single-int-list", MIN_OF, Arrays.asList( 1 ), Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-single-long-list", MIN_OF, Arrays.asList( 1L ), Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-single-double-list", MIN_OF, Arrays.asList( 1.0 ), Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-single-int-array-extra-arg", MIN_OF, new Object[] {1, "a"}, Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-single-long-array-extra-arg", MIN_OF, new Object[] {1L, "a"}, Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-single-double-array-extra-arg", MIN_OF, new Object[] {1.0, "a"}, Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-single-int-list-extra-arg", MIN_OF, Arrays.asList( 1, "a" ), Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-single-long-list-extra-arg", MIN_OF, Arrays.asList( 1L, "a" ), Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-single-double-list-extra-arg", MIN_OF, Arrays.asList( 1.0, "a" ), Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-multi-int-array", MIN_OF, new Object[] {1, 3, 2, 5}, Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-multi-long-array", MIN_OF, new Object[] {1L, 3L, 2L, 5L}, Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-multi-double-array", MIN_OF, new Object[] {1.0, 3.0, 2.0, 5.0}, Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-multi-int-list", MIN_OF, Arrays.asList( 1, 3, 2, 5 ), Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-multi-long-list", MIN_OF, Arrays.asList( 1L, 3L, 2L, 5L ), Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-multi-double-list", MIN_OF, Arrays.asList( 1.0, 3.0, 2.0, 5.0 ), Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-combo-int-array", MIN_OF, new Object[] {1, 3L, null, 5.0}, Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-combo-long-array", MIN_OF, new Object[] {1L, 3, null, 5.0}, Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-combo-double-array", MIN_OF, new Object[] {1.0, 3L, null, 5}, Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-combo-int-list", MIN_OF, Arrays.asList( 1, 3L, null, 5.0 ), Optional.of( 1 ) } );
        testCases.add( new Object[] { "min-combo-long-list", MIN_OF, Arrays.asList( 1L, 3, null, 5.0 ), Optional.of( 1L ) } );
        testCases.add( new Object[] { "min-combo-double-list", MIN_OF, Arrays.asList( 1.0, 3L, null, 5 ), Optional.of( 1.0 ) } );

        testCases.add( new Object[] { "min-NaN", MIN_OF, Arrays.asList( -1.0, Double.NaN ), Optional.of( Double.NaN ) } );
        testCases.add( new Object[] { "min-negative-Infinity", MIN_OF, Arrays.asList( -1.0, Double.NEGATIVE_INFINITY ), Optional.of( Double.NEGATIVE_INFINITY ) } );
        testCases.add( new Object[] { "min-NaN-positive-infinity", MIN_OF, Arrays.asList( -1.0, Double.NaN, Double.NEGATIVE_INFINITY ), Optional.of( Double.NaN ) } );


        testCases.add( new Object[] { "abs-null", ABS_OF, null, Optional.empty() } );
        testCases.add( new Object[] { "abs-invalid", ABS_OF, new Object(), Optional.empty() } );
        testCases.add( new Object[] { "abs-empty-list", ABS_OF, new Object[] {}, Optional.empty() } );
        testCases.add( new Object[] { "abs-empty-array", ABS_OF, Arrays.asList(  ), Optional.empty() } );

        testCases.add( new Object[] { "abs-single-negative-int", ABS_OF, -1, Optional.of( 1 ) } );
        testCases.add( new Object[] { "abs-single-negative-long", ABS_OF, -1L, Optional.of(1L) } );
        testCases.add( new Object[] { "abs-single-negative-double", ABS_OF, -1.0, Optional.of(1.0) } );
        testCases.add( new Object[] { "abs-single-positive-int", ABS_OF, 1, Optional.of( 1 ) } );
        testCases.add( new Object[] { "abs-single-positive-long", ABS_OF, 1L, Optional.of(1L) } );
        testCases.add( new Object[] { "abs-single-positive-double", ABS_OF, 1.0, Optional.of(1.0) } );

        testCases.add( new Object[] { "abs-list", ABS_OF, new Object[] { -1, -1L, -1.0 }, Optional.of( Arrays.asList( 1, 1L, 1.0 ) ) } );
        testCases.add( new Object[] { "abs-array", ABS_OF, Arrays.asList( -1, -1L, -1.0 ), Optional.of( Arrays.asList( 1, 1L, 1.0 ) ) } );

        testCases.add( new Object[] { "abs-Nan", ABS_OF, Double.NaN, Optional.of(Double.NaN) } );
        testCases.add( new Object[] { "abs-PosInfinity", ABS_OF, Double.POSITIVE_INFINITY, Optional.of(Double.POSITIVE_INFINITY) } );
        testCases.add( new Object[] { "abs-NefInfinity", ABS_OF, Double.NEGATIVE_INFINITY, Optional.of(Double.POSITIVE_INFINITY) } );


        testCases.add( new Object[] { "toInt-null", TO_INTEGER, null, Optional.empty() } );
        testCases.add( new Object[] { "toInt-invalid", TO_INTEGER, new Object(), Optional.empty() } );
        testCases.add( new Object[] { "toInt-empty-array", TO_INTEGER, new Object[] {}, Optional.empty() } );
        testCases.add( new Object[] { "toInt-empty-list", TO_INTEGER, Arrays.asList(  ), Optional.empty() } );

        testCases.add( new Object[] { "toInt-single-positive-string", TO_INTEGER, "1", Optional.of( 1 ) } );
        testCases.add( new Object[] { "toInt-single-negative-string", TO_INTEGER, "-1", Optional.of( -1 ) } );
        testCases.add( new Object[] { "toInt-single-positive-int", TO_INTEGER, 1, Optional.of( 1 ) } );
        testCases.add( new Object[] { "toInt-single-negative-int", TO_INTEGER, -1, Optional.of( -1 ) } );
        testCases.add( new Object[] { "toInt-single-positive-long", TO_INTEGER, 1L, Optional.of( 1 ) } );
        testCases.add( new Object[] { "toInt-single-negative-long", TO_INTEGER, -1L, Optional.of( -1 ) } );
        testCases.add( new Object[] { "toInt-single-positive-double", TO_INTEGER, 1.0, Optional.of( 1 ) } );
        testCases.add( new Object[] { "toInt-single-negative-double", TO_INTEGER, -1.0, Optional.of( -1 ) } );

        testCases.add( new Object[] { "toInt-single-positive-string-list", TO_INTEGER, new Object[] {"1", "2"}, Optional.of( Arrays.asList( 1, 2 ) ) } );
        testCases.add( new Object[] { "toInt-single-negative-string-array", TO_INTEGER, Arrays.asList( "-1", "-2" ), Optional.of( Arrays.asList( -1, -2 ) ) } );
        testCases.add( new Object[] { "toInt-single-positive-int-list", TO_INTEGER, new Object[] { 1, 2 }, Optional.of( Arrays.asList( 1, 2 ) ) } );
        testCases.add( new Object[] { "toInt-single-negative-int-array", TO_INTEGER, Arrays.asList( -1, -2 ), Optional.of( Arrays.asList( -1, -2 ) ) } );
        testCases.add( new Object[] { "toInt-single-positive-long-list", TO_INTEGER, new Object[] {1L, 2L}, Optional.of( Arrays.asList( 1, 2 ) ) } );
        testCases.add( new Object[] { "toInt-single-negative-long-array", TO_INTEGER, Arrays.asList( -1L, -2L ), Optional.of( Arrays.asList( -1, -2 ) ) } );
        testCases.add( new Object[] { "toInt-single-positive-double-list", TO_INTEGER, new Object[] {1.0, 2.0}, Optional.of( Arrays.asList( 1, 2 ) ) } );
        testCases.add( new Object[] { "toInt-single-negative-double-array", TO_INTEGER, Arrays.asList( -1.0, -2.0 ), Optional.of( Arrays.asList( -1, -2 ) ) } );


        testCases.add( new Object[] { "toDouble-null", TO_DOUBLE, null, Optional.empty() } );
        testCases.add( new Object[] { "toDouble-invalid", TO_DOUBLE, new Object(), Optional.empty() } );
        testCases.add( new Object[] { "toDouble-empty-array", TO_DOUBLE, new Object[] {}, Optional.empty() } );
        testCases.add( new Object[] { "toDouble-empty-list", TO_DOUBLE, Arrays.asList(  ), Optional.empty() } );

        testCases.add( new Object[] { "toDouble-single-positive-string", TO_DOUBLE, "1", Optional.of( 1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-negative-string", TO_DOUBLE, "-1", Optional.of( -1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-positive-int", TO_DOUBLE, 1, Optional.of( 1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-negative-int", TO_DOUBLE, -1, Optional.of( -1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-positive-long", TO_DOUBLE, 1L, Optional.of( 1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-negative-long", TO_DOUBLE, -1L, Optional.of( -1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-positive-double", TO_DOUBLE, 1.0, Optional.of( 1.0 ) } );
        testCases.add( new Object[] { "toDouble-single-negative-double", TO_DOUBLE, -1.0, Optional.of( -1.0 ) } );

        testCases.add( new Object[] { "toDouble-single-positive-string-list", TO_DOUBLE, new Object[] {"1", "2"}, Optional.of( Arrays.asList( 1.0, 2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-negative-string-array", TO_DOUBLE, Arrays.asList( "-1", "-2" ), Optional.of( Arrays.asList( -1.0, -2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-positive-int-list", TO_DOUBLE, new Object[] { 1, 2 }, Optional.of( Arrays.asList( 1.0, 2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-negative-int-array", TO_DOUBLE, Arrays.asList( -1, -2 ), Optional.of( Arrays.asList( -1.0, -2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-positive-long-list", TO_DOUBLE, new Object[] {1L, 2L}, Optional.of( Arrays.asList( 1.0, 2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-negative-long-array", TO_DOUBLE, Arrays.asList( -1L, -2L ), Optional.of( Arrays.asList( -1.0, -2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-positive-double-list", TO_DOUBLE, new Object[] {1.0, 2.0}, Optional.of( Arrays.asList( 1.0, 2.0 ) ) } );
        testCases.add( new Object[] { "toDouble-single-negative-double-array", TO_DOUBLE, Arrays.asList( -1.0, -2.0 ), Optional.of( Arrays.asList( -1.0, -2.0 ) ) } );


        testCases.add( new Object[] { "toLong-null", TO_LONG, null, Optional.empty() } );
        testCases.add( new Object[] { "toLong-invalid", TO_LONG, new Object(), Optional.empty() } );
        testCases.add( new Object[] { "toLong-empty-array", TO_LONG, new Object[] {}, Optional.empty() } );
        testCases.add( new Object[] { "toLong-empty-list", TO_LONG, Arrays.asList(  ), Optional.empty() } );

        testCases.add( new Object[] { "toLong-single-positive-string", TO_LONG, "1", Optional.of( 1L ) } );
        testCases.add( new Object[] { "toLong-single-negative-string", TO_LONG, "-1", Optional.of( -1L ) } );
        testCases.add( new Object[] { "toLong-single-positive-int", TO_LONG, 1, Optional.of( 1L ) } );
        testCases.add( new Object[] { "toLong-single-negative-int", TO_LONG, -1, Optional.of( -1L ) } );
        testCases.add( new Object[] { "toLong-single-positive-long", TO_LONG, 1L, Optional.of( 1L ) } );
        testCases.add( new Object[] { "toLong-single-negative-long", TO_LONG, -1L, Optional.of( -1L ) } );
        testCases.add( new Object[] { "toLong-single-positive-double", TO_LONG, 1L, Optional.of( 1L ) } );
        testCases.add( new Object[] { "toLong-single-negative-double", TO_LONG, -1L, Optional.of( -1L ) } );

        testCases.add( new Object[] { "toLong-single-positive-string-list", TO_LONG, new Object[] {"1", "2"}, Optional.of( Arrays.asList( 1L, 2L ) ) } );
        testCases.add( new Object[] { "toLong-single-negative-string-array", TO_LONG, Arrays.asList( "-1", "-2" ), Optional.of( Arrays.asList( -1L, -2L ) ) } );
        testCases.add( new Object[] { "toLong-single-positive-int-list", TO_LONG, new Object[] { 1, 2 }, Optional.of( Arrays.asList( 1L, 2L ) ) } );
        testCases.add( new Object[] { "toLong-single-negative-int-array", TO_LONG, Arrays.asList( -1, -2 ), Optional.of( Arrays.asList( -1L, -2L ) ) } );
        testCases.add( new Object[] { "toLong-single-positive-long-list", TO_LONG, new Object[] {1L, 2L}, Optional.of( Arrays.asList( 1L, 2L ) ) } );
        testCases.add( new Object[] { "toLong-single-negative-long-array", TO_LONG, Arrays.asList( -1L, -2L ), Optional.of( Arrays.asList( -1L, -2L ) ) } );
        testCases.add( new Object[] { "toLong-single-positive-double-list", TO_LONG, new Object[] {1L, 2L}, Optional.of( Arrays.asList( 1L, 2L ) ) } );
        testCases.add( new Object[] { "toLong-single-negative-double-array", TO_LONG, Arrays.asList( -1L, -2L ), Optional.of( Arrays.asList( -1L, -2L ) ) } );

        testCases.add( new Object[] { "toInteger-combo-string-array", TO_INTEGER, Arrays.asList( "-1", 2, -3L, 4.0 ), Optional.of( Arrays.asList( -1, 2, -3, 4 ) ) } );
        testCases.add( new Object[] { "toLong-combo-int-array",       TO_LONG,    Arrays.asList( "-1", 2, -3L, 4.0 ), Optional.of( Arrays.asList( -1L, 2L, -3L, 4L ) ) } );
        testCases.add( new Object[] { "toDouble-combo-long-array",    TO_DOUBLE,  Arrays.asList( "-1", 2, -3L, 4.0 ), Optional.of( Arrays.asList( -1.0, 2.0, -3.0, 4.0 ) ) } );

        testCases.add( new Object[] { "intsum-combo-string-array",    INT_SUM_OF,  Arrays.asList(1, 2.0, "random", 0), Optional.of(3)});
        testCases.add( new Object[] { "intsum-single-value",          INT_SUM_OF,  2,                                  Optional.empty()});
        testCases.add( new Object[] { "intsum-combo-intstring-array", INT_SUM_OF,  Arrays.asList(1L, 2, "-3.0", 0), Optional.of(0)});

        testCases.add( new Object[] { "doublesum-combo-string-array",    DOUBLE_SUM_OF, Arrays.asList(1, 2.0, "random", 0), Optional.of(3.0)});
        testCases.add( new Object[] { "doublesum-single-value",          DOUBLE_SUM_OF, 2,                                  Optional.empty()});
        testCases.add( new Object[] { "doublesum-combo-intstring-array", DOUBLE_SUM_OF, Arrays.asList(1L, 2, "-3.0", 0),    Optional.of(0.0)});

        testCases.add( new Object[] { "longsum-combo-string-array",      LONG_SUM_OF,   Arrays.asList(1, 2.0, "random", 0), Optional.of(3L)});
        testCases.add( new Object[] { "longsum-single-value",            LONG_SUM_OF,   2, Optional.empty()});
        testCases.add( new Object[] { "longsum-combo-intstring-array",   LONG_SUM_OF,   Arrays.asList(1L, 2, "-3.0", 0),    Optional.of(0L)});

        testCases.add( new Object[] { "intsubtract-happy-path",      INT_SUBTRACT_OF,  Arrays.asList(4, 1),  Optional.of(3)});
        testCases.add( new Object[] { "intsubtract-single-value",    INT_SUBTRACT_OF,  2,                    Optional.empty()});
        testCases.add( new Object[] { "intsubtract-wrong-type",      INT_SUBTRACT_OF,  Arrays.asList(4L, 1), Optional.empty()});

        testCases.add( new Object[] { "doublesubtract-happy-path",   DOUBLE_SUBTRACT_OF,  Arrays.asList(4.0, 1.0),  Optional.of(3.0)});
        testCases.add( new Object[] { "doublesubtract-single-value", DOUBLE_SUBTRACT_OF,  2.0,                      Optional.empty()});
        testCases.add( new Object[] { "doublesubtract-wrong-type",   DOUBLE_SUBTRACT_OF,  Arrays.asList(4L, 1),     Optional.empty()});

        testCases.add( new Object[] { "longsubtract-happy-path",     LONG_SUBTRACT_OF,  Arrays.asList(4L, 1L),  Optional.of(3L)});
        testCases.add( new Object[] { "longsubtract-single-value",   LONG_SUBTRACT_OF,  2L,                     Optional.empty()});
        testCases.add( new Object[] { "longsubtract-wrong-type",     LONG_SUBTRACT_OF,  Arrays.asList(4.0, 1),  Optional.empty()});

        // Test to make sure "div" only uses the first and second element in the array and ignores the rest.
        testCases.add( new Object[] { "div-combo-array",          DIV_OF, Arrays.asList(10L, 5.0, 2), Optional.empty()});
        testCases.add( new Object[] { "div-combo-string-array",   DIV_OF, Arrays.asList(10L, "5", 2), Optional.empty()});
        testCases.add( new Object[] { "div-single-element-array", DIV_OF, Arrays.asList("5"),         Optional.empty()});
        testCases.add( new Object[] { "div-single-element",       DIV_OF, "10",                       Optional.empty()});

        // Dividing by 0 returns an empty result.
        testCases.add( new Object[] { "div-combo-invalid-array",  DIV_OF, Arrays.asList(10L, 0, 2),   Optional.empty()});

        // Dividing 0 by any number returns 0.0(double)
        testCases.add( new Object[] { "div-combo-valid-array",    DIV_OF, Arrays.asList(0.0,  10), Optional.of(0.0)});

        testCases.add( new Object[] { "divAndRound-single-precision-array",      DIV_AND_ROUND_OF, Arrays.asList(1, 5.0, 2), Optional.of(2.5)});
        testCases.add( new Object[] { "divAndRound-double-precision-array",      DIV_AND_ROUND_OF, Arrays.asList(2, 5.0, 2), Optional.of(2.50)});
        testCases.add( new Object[] { "divAndRound-trailing-precision-array",    DIV_AND_ROUND_OF, Arrays.asList(3, 5.0, 2), Optional.of(2.500)});
        testCases.add( new Object[] { "divAndRound-no-precision-array",          DIV_AND_ROUND_OF, Arrays.asList(0, 5.0, 2), Optional.of(3.0)}); // Round up as >= 0.5
        testCases.add( new Object[] { "divAndRound-no-precision-array",          DIV_AND_ROUND_OF, Arrays.asList(0, 4.8, 2), Optional.of(2.0)}); // Round down as < 0.5
        
        testCases.add( new Object[] { "multiply-combo-array",          MULTIPLY_OF, Arrays.asList(30, 2), Optional.of(60)}); 
        
        return testCases.iterator();
    }

    @Test
    @SuppressWarnings( "all" )
    public void testNitPicks() {
        // we want to be able to return the min/max element of input type, not
        // autoboxed type  -- wanted to return int (2), returned double (2.0)
        Object c = (1.0 > 2 ? 1.0   : 2);
        assert c.getClass() == Double.class && c.equals( 2.0 );

        // toNumber parsing preference ordering (int-then-long-then-double) demo
        assert toNumber("123").equals( Optional.of( 123 ) );
        assert toNumber("123123123123123123").equals( Optional.of( 123123123123123123l ) );
        assert toNumber("123123123123123123123123123123123123").equals( Optional.of( 123123123123123123123123123123123123d ) );

        // abs returns numbers in their appropriate type, not given type (string in this case)
        assert abs( "-123" ).equals( Optional.of( 123 ));
        assert abs("-123123123123123123").equals( Optional.of( 123123123123123123l ) );
        assert abs("-123123123123123123123123123123123123").equals( Optional.of( 123123123123123123123123123123123123d ) );
    }
}
