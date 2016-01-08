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
package com.bazaarvoice.jolt;

import java.util.List;
import java.util.Map;

/**
 * Subclass of Diffy that does not care about JSON Array order.
 *
 * Useful for diffing JSON created from Java Tools that do not
 *  care about preserving JSON array order from call to call.
 *  *cough* DevAPI *cough*
 */
public class ArrayOrderObliviousDiffy extends Diffy {

    public ArrayOrderObliviousDiffy(JsonUtil jsonUtil) {
        super(jsonUtil);
    }

    public ArrayOrderObliviousDiffy() {super();}

    @Override
    protected Result diffList(List<Object> expected, List<Object> actual) {

        // First we got thru an n^2 operation to compare the two lists
        for (int expectedIndex=0; expectedIndex < expected.size(); expectedIndex++) {

            Object exp = expected.get(expectedIndex);

            for(int actualIndex=0; actualIndex < actual.size(); actualIndex++) {
                Object act = actual.get(actualIndex);

                if ( exp == null && act == null ) {
                    // great, we "found a match"
                    break;
                }

                if( act != null && exp != null ) {

                    // Ideally the equals method finds a match, works for identical maps and simple Strings and numbers
                    // Also try the sub-classable diffScalar if the normal ".equals" does not work
                    if ( act.equals(exp) || diffScalar( exp, act ).isEmpty() ) {
                        // if the indicies match nuke them
                        expected.set(expectedIndex, null);
                        actual.set(actualIndex, null);
                        break;
                    }
                    else if ( (exp instanceof List && act instanceof List) ||
                              (exp instanceof Map  && act instanceof Map) ) {

                        // ugh, n^2 again, but enter from the top so a copy is made
                        Diffy.Result result = diff( exp, act );
                        if ( result.isEmpty() ) {
                            // score!
                            expected.set(expectedIndex, null);
                            actual.set(actualIndex, null);
                            break;
                        }
                    }
                }
            }
        }

        // See if all the indicies in the arrays were nulled out.
        if ( isAllNulls( expected ) && isAllNulls( actual ) ) {
            return new Result();
        }

        // Now we make a second pass, "lining up" and subtractively Diffy-ing non-null elements.
        int actualIndex = 0;

        for (int expectedIndex=0; expectedIndex < expected.size() && actualIndex < actual.size(); expectedIndex++) {

            expectedIndex = findNextNonNullIndex( expected, expectedIndex );
            if ( expectedIndex >= expected.size() ) {
                break;
            }

            actualIndex = findNextNonNullIndex( actual, actualIndex );
            if ( actualIndex >= actual.size() ) {
                break;
            }

            Object exp = expected.get(expectedIndex);
            Object act = actual.get(actualIndex);

            // Do an actual "subtractive" diff, with "lined up" non-null items
            Result subResult = diffHelper( exp, act );
            expected.set( expectedIndex, subResult.expected );
            actual.set( actualIndex, subResult.actual );

            actualIndex++;
        }

        return new Result( expected, actual );
    }

    private boolean isAllNulls( List<Object> list ) {

        boolean isAllNulls = true;
        for( int index=0; isAllNulls && index < list.size(); index++) {
            if ( list.get(index) != null ) {
                isAllNulls = false;
            }
        }
        return isAllNulls;
    }

    private static int findNextNonNullIndex( List<Object> list, int index ) {

        while ( index < list.size() ) {

            if (list.get(index) != null ) {
                break;
            }

            index++;
        }
        return index;
    }
}
