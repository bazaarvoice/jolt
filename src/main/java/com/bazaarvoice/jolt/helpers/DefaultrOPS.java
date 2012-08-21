package com.bazaarvoice.jolt.helpers;

import com.bazaarvoice.jolt.Defaultr;

import java.util.Comparator;

public enum DefaultrOPS {

    STAR, OR, LITERAL;

    public static DefaultrOPS parse( String key ) {
        if ( key.contains( Defaultr.WildCards.STAR ) ){
            return STAR;
        }
        if ( key.contains( Defaultr.WildCards.OR ) ) {
            return OR;
        }
        return LITERAL;
    }

    public static class OpsPrecedenceComparator implements Comparator<DefaultrOPS> {
        /**
         * The order we want to apply Defaultr logic is Literals, Or, and then Star.
         * Since we walk the sorted data from 0 to n, that means Literals need to low, and Star should be high.
         */
        @Override
        public int compare(DefaultrOPS ops, DefaultrOPS ops1) {

            // a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater than the second.
            // s < s1 -> -1
            // s = s1 -> 0
            // s > s1 -> 1

            if ( ops == ops1 ) {
                return 0;
            }

            if ( STAR == ops ) {
                return 1;
            }
            if ( LITERAL == ops ) {
                return -1;
            }

            // op has to be OR if we get here
            if ( STAR == ops1) {
                return -1;
            }
            if ( LITERAL == ops1 ) {
                return 1;
            }
            return 0; // both are ORs, should never get here
        }
    }
}
