package com.bazaarvoice.jolt.defaultr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayKey extends Key<Integer> {

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
    public Collection<Integer> getKeyValues() {
        return keyInts;
    }

    @Override
    public int getLiteralIntKey() {
        return keyInt;
    }

    @Override
    public Collection<Integer> findMatchingDefaulteeKeys( Object defaultee ) {

        if ( defaultee == null ) {
            return Collections.emptyList();
        }

        switch ( getOp() ) {
            // If the Defaultee is not null, it should get these literal values added to it
            case LITERAL:
                return getKeyValues();
            // Identify all its keys
            case STAR:
                if ( defaultee instanceof List ) {
                    // this assumes the defaultee list has already been expanded to the right size
                    List defaultList = (List) defaultee;
                    List<Integer> allIndexes = new ArrayList<Integer>( defaultList.size() );
                    for ( int index = 0; index < defaultList.size(); index++ ) {
                        allIndexes.add( index );
                    }

                    return allIndexes;
                }
                break;
            // Identify the intersection between its keys and the OR values
            case OR:
                if ( defaultee instanceof List ) {
                    List<Integer> indexesInRange = new ArrayList<Integer>();

                    for ( Integer orValue : getKeyValues() ) {
                        if ( orValue < ((List)defaultee).size() ) {
                            indexesInRange.add( orValue );
                        }
                    }
                    return indexesInRange;
                }
                break;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }

        return Collections.emptyList();
    }
}
