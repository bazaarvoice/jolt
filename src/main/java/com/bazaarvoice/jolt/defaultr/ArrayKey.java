package com.bazaarvoice.jolt.defaultr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayKey extends Key<Integer, List<Object>> {

    private Collection<Integer> keyInts;
    private int keyInt = -1;

    public ArrayKey( String jsonKey, Object spec ) {
        super( jsonKey, spec );

        // Handle ArrayKey specific stuff
        switch( getOp() ){
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
    protected Collection<Integer> getKeyValues() {
        return keyInts;
    }

    @Override
    protected int getLiteralIntKey() {
        return keyInt;
    }

    @Override
    protected void applyChild( Object container ) {

        if ( container instanceof List ) {
            List<Object> defaultList = (List<Object>) container;

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( Integer literalKey : determineMatchingContainerKeys( defaultList ) ) {
                applyLiteralKeyToContainer( literalKey, defaultList );
            }
        }
        // Else there is disagreement (with respect to Array vs Map) between the data in
        //  the container vs the Defaultr Spec type for this key.  Container wins so do nothing.
    }

    @Override
    protected void applyLiteralKeyToContainer( Integer literalIndex, List<Object> container ) {

        Object defaulteeValue = container.get( literalIndex );

        if ( children == null ) {
            if ( defaulteeValue == null ) {
                container.set( literalIndex, literalValue );  // apply a default value into a List, assumes the list as already been expanded if needed.
            }
        }
        else {
            if ( defaulteeValue == null ) {
                defaulteeValue = createOutputContainerObject();
                container.set( literalIndex, defaulteeValue ); // push a new sub-container into this list
            }

            // recurse by applying my children to this known valid container
            applyChildren( defaulteeValue );
        }
    }

    @Override
    protected Collection<Integer> determineMatchingContainerKeys( List<Object> container ) {

        switch ( getOp() ) {
            case LITERAL:
                // Container it should get these literal values added to it
                return getKeyValues();
            case STAR:
                // Identify all its keys
                // this assumes the container list has already been expanded to the right size
                List defaultList = (List) container;
                List<Integer> allIndexes = new ArrayList<Integer>( defaultList.size() );
                for ( int index = 0; index < defaultList.size(); index++ ) {
                    allIndexes.add( index );
                }

                return allIndexes;
            case OR:
                // Identify the intersection between the container "keys" and the OR values
                List<Integer> indexesInRange = new ArrayList<Integer>();

                for ( Integer orValue : getKeyValues() ) {
                    if ( orValue < ((List) container ).size() ) {
                        indexesInRange.add( orValue );
                    }
                }
                return indexesInRange;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }
}
