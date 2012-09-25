package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.Defaultr;

import java.util.Comparator;

public enum OPS {

    STAR, OR, AT, LITERAL;

    public static OPS parse( String key ) {
        if ( key.contains( Key.WildCards.STAR ) ){
            return STAR;
        }
        if ( key.contains( Key.WildCards.OR ) ) {
            return OR;
        }
        if ( key.contains( Key.WildCards.AT) ){
            return AT;
        }
        return LITERAL;
    }

    public static class OpsPrecedenceComparator implements Comparator<OPS> {
        /**
         * The order we want to apply Defaultr logic is Literals, Or, and then Star.
         * Since we walk the sorted data from 0 to n, that means Literals need to low, and Star should be high.
         */
        @Override
        public int compare(OPS ops, OPS ops1) {

            // a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
            // s < s1 -> -1
            // s = s1 -> 0
            // s > s1 -> 1

            if ( ops == ops1 ) {
                return 0;
            }

            // AT   STAR   OR    LITERAL

            if ( AT == ops ) {    // high end
                return 1;
            }
            if ( LITERAL == ops ) { // low end
                return -1;
            }

            // if we get here, "ops" has to equal OR or STAR
            if( STAR == ops ) {

                if ( AT == ops1) {
                    return -1;
                }
                return 1;  // ops1 can't be STAR or AT, so must be OR or LITERAL
            }
            else { // OR == ops
                if ( LITERAL == ops1 ) {
                    return 1;
                }
                return -1; // ops1 can't be OR or LITERAL, so must be AT or STAR
            }
        }
    }
}
