package com.bazaarvoice.jolt.defaultr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class ArrayKey extends Key {

    private Collection<Integer> keyInts;
    private int keyInt = -1;

    public ArrayKey( String jsonKey ) {

        super.init( jsonKey );

        //// FIGURE OUT WHAT THE keyValues ARE
        switch( op ){
            case OR :
                keyInts = new ArrayList<Integer>();
                for( String orLiteral : keyStrings ) {
                    int orInt = Integer.parseInt( orLiteral );
                    keyInts.add( orInt );
                }
                break;
            case LITERAL:
                keyInt = Integer.parseInt( rawKey );
                keyInts = Arrays.asList( keyInt );
                break;
            case STAR:
                keyInts = Collections.emptyList();
                break;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }

    @Override
    public Collection getKeyValues() {
        return keyInts;
    }

    @Override
    public int getLiteralIntKey() {
        return keyInt;
    }
}
