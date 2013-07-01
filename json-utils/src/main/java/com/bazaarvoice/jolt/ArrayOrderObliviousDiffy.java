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
 * Subclass of Diffy that does not care about Json Array order.
 *
 * Useful for diffing Json created from Java Tools that do not
 *  care about preserving Json array order from call to call.
 *  *cough* DevAPI *cough*
 */
public class ArrayOrderObliviousDiffy extends Diffy {

    protected Result diffList(List expected, List actual) {
        Result result = super.diffList( expected, actual );
        if (result.isEmpty()) {
            return result;
        }
        for (int i=expected.size()-1; i>=0; i--) {
            int idx = actual.indexOf( expected.get( i ) );
            if (idx >= 0) {
                expected.remove( i );
                actual.remove( idx );
            }
        }
        if (expected.isEmpty() && actual.isEmpty()) {
            return new Result();
        }
        return new Result( expected, actual );
    }
}
