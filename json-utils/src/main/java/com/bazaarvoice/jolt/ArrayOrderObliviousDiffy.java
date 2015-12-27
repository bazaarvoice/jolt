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

/**
 * Subclass of Diffy that does not care about JSON Array order.
 *
 * Useful for diffing JSON created from Java Tools that do not
 *  care about preserving JSON array order from call to call.
 *  *cough* DevAPI *cough*
 */
public class ArrayOrderObliviousDiffy extends Diffy {

    @Override
    protected Result diffList(List expected, List actual) {

        for (int i=0; i < expected.size(); i++) {

            Object exp = expected.get(i);

            /**
             * iterate through all the items in list and see any of them matches with it
             * contains() does the same thing, but checks only equals()
             * we have to check if either, equals() or hashcode() or our own diff()
             * returns that they are same
             *
             * this applies to Lists with similar but differently ordered data set
             * i.e. [[1,2]].contains([2,1]) == false
             *
             * this is expensive, but necessary!
             */
            for(int j=0; j< actual.size(); j++) {
                Object act = actual.get(j);
                if( (act != null && exp != null) &&
                    (act.equals(exp) || act.hashCode() == exp.hashCode() || diff(exp, act).isEmpty() ) )
                {
                    expected.remove(i);
                    actual.remove(j);

                    // The expected.remove(i) will slide things down
                    // reduce the count of the outer loop, so that the i++ gets back to the "same place"
                    i--;
                    break;
                }
            }
        }
        if (expected.isEmpty() && actual.isEmpty()) {
            return new Result();
        }
        return new Result( expected, actual );
    }
}
