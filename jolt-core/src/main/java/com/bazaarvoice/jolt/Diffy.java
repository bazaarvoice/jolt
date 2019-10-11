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
 * JSON Diff tool that will walk two "JSON" objects simultaneously and identify mismatches.
 *
 * Algorithm :
 *   1) make a copy of both input objects
 *   2) walk both objects and _remove_ items that match
 *   3) return what is left of the two objects in the Result
 *
 * In the case a full / "sucessful" match, Diffy returns a Result object with isEmpty() == true.
 */
public class Diffy {

    private final JsonUtil jsonUtil;

    public Diffy() {
        jsonUtil = JsonUtils.getDefaultJsonUtil();
    }

    /**
     * Pass in a custom jsonUtil to use for the cloneJson method.
     */
    public Diffy( JsonUtil jsonUtil ) {
        this.jsonUtil = jsonUtil;
    }

    public Result diff(Object expected, Object actual) {
        Object expectedCopy = jsonUtil.cloneJson( expected );
        Object actualCopy = jsonUtil.cloneJson( actual );
        return diffHelper( expectedCopy, actualCopy );
    }

    @SuppressWarnings( "unchecked" )
    protected Result diffHelper(Object expected, Object actual) {
        if (expected instanceof Map) {
            if (!(actual instanceof Map)) {
                return new Result( expected, actual );
            }
            return diffMap( (Map<String, Object>) expected, (Map<String, Object>) actual );
        }
        else if (expected instanceof List) {
            if (!(actual instanceof List)) {
                return new Result( expected, actual );
            }
            return diffList( (List<Object>) expected, (List<Object>) actual );
        }
        return this.diffScalar( expected, actual );
    }

    protected Result diffMap(Map<String, Object> expected, Map<String, Object> actual) {

        // Make a copy of the expected keySet so that we can remove things w/out concurrent mod exceptions
        String[] expectedKeys = expected.keySet().toArray( new String[ expected.keySet().size() ] );
        for (String key : expectedKeys ) {
            Result subResult = diffHelper( expected.get( key ), actual.get( key ) );
            if (subResult.isEmpty()) {
                expected.remove( key );
                actual.remove( key );
            }
        }
        if (expected.isEmpty() && actual.isEmpty()) {
            return new Result();
        }
        return new Result( expected, actual );
    }

    protected Result diffList(List<Object> expected, List<Object> actual) {
        int shortlen = Math.min( expected.size(), actual.size() );
        boolean emptyDiff = true;
        for (int i=0; i<shortlen; i++) {
            Result subresult = diffHelper( expected.get( i ), actual.get( i ) );
            expected.set( i, subresult.expected );
            actual.set( i, subresult.actual );
            emptyDiff = emptyDiff && subresult.isEmpty();
        }
        if (emptyDiff && (expected.size() == actual.size())) {
            return new Result();
        }
        return new Result( expected, actual );
    }

    protected Result diffScalar(Object expected, Object actual) {
        if (expected == null) {
            if (actual == null) {
                return new Result();                    // both null, isEmpty diff
            }
            return new Result( null, actual );      // one is null, full diff
        }
        if (actual == null) {
            return new Result( expected, null );      // one is null, full diff
        }
        if (scalarEquals( expected, actual ) ) {
            return new Result();                        // equivalent, isEmpty diff
        }
        return new Result( expected, actual );          // non-equivalent, full diff
    }

    /**
     * Allow subclasses to handle things like Long 0 versus Int 0.  They should be the same,
     *  but the .equals doesn't handle it.
     */
    protected boolean scalarEquals( Object expected, Object actual ) {
        return expected.equals( actual );
    }

    /**
     * Contains the unmatched fields from the Diffy operation.
     *
     * A sucessful/identical match returns isEmpty() == true.
     */
    public static class Result {
        public Object expected;
        public Object actual;
        public Result() {}
        public Result(Object expected, Object actual) {
            this.expected = expected;
            this.actual = actual;
        }
        public boolean isEmpty() {
            return (expected == null) && (actual == null);
        }

        @Override
        public String toString() {
            if(isEmpty()) {
                return "There is no difference!";
            }
            else {
                return "\nExpected:\n" + JsonUtils.toPrettyJsonString(expected) + "\n" +
                       "\nActual\n" + JsonUtils.toPrettyJsonString(actual);
            }
        }
    }
}
