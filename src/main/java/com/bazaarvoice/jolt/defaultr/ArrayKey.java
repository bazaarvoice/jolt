package com.bazaarvoice.jolt.defaultr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ArrayKey extends Key<Integer, List<Object>> {

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
    public void applySubSpec( Object subSpec, Object defaultee ) {

        if ( defaultee instanceof List ) {

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( Integer literalKey : findMatchingDefaulteeKeys( defaultee ) ) {
                defaultLiteralValue( literalKey, subSpec, (List<Object>) defaultee );
            }
        }
        // Else defaultee was not a container object, the wrong type of container object, or null
        //  net effect, we couldn't push values into it
    }

    @Override
    public void defaultLiteralValue( Integer literalIndex, Object subSpec, List<Object> defaultee ) {

        Object defaulteeValue = defaultee.get( literalIndex );

        if ( subSpec instanceof Map ) {
            if ( defaulteeValue == null ) {
                defaulteeValue = createDefaultContainerObject();
                defaultee.set( literalIndex, defaulteeValue ); // push a new sub-container into this list
            }

            // Re-curse into subspec
            applySpec( (Map<Key, Object>) subSpec, defaulteeValue );
        } else {
            if ( defaulteeValue == null ) {
                defaultee.set( literalIndex, subSpec );  // apply a default value into a List, assumes the list as already been expanded if needed.
            }
        }
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
