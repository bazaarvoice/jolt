package com.bazaarvoice.concierge.tools;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nate.forman
 * Date: 8/3/12
 * Time: 11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class Diffy {

    public Result diff(Object expected, Object actual) {
        Object expectedCopy = JsonUtils.cloneJson( expected );
        Object actualCopy = JsonUtils.cloneJson( actual );
        return diffHelper( expectedCopy, actualCopy );
    }

    private Result diffHelper(Object expected, Object actual) {
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
            return diffList( (List) expected, (List) actual );
        }
        return this.diffScalar( expected, actual );
    }

    private Result diffMap(Map<String, Object> expected, Map<String, Object> actual) {
        for (Object key: expected.keySet().toArray()) {
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

    private Result diffList(List expected, List actual) {
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

    private Result diffScalar(Object expected, Object actual) {
        if (expected == null) {
            if (actual == null) {
                return new Result();                    // both null, isEmpty diff
            }
            return new Result( expected, actual );      // one is null, full diff
        }
        if (actual == null) {
            return new Result( expected, actual );      // one is null, full diff
        }
        if (expected.equals( actual )) {
            return new Result();                        // equivalent, isEmpty diff
        }
        return new Result( expected, actual );          // non-equivalent, full diff
    }

    public static class Result {
        public Object expected;
        public Object actual;
        Result() {}
        Result(Object expected, Object actual) {
            this.expected = expected;
            this.actual = actual;
        }
        public boolean isEmpty() {
            return (expected == null) && (actual == null);
        }
    }
}
