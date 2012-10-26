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
