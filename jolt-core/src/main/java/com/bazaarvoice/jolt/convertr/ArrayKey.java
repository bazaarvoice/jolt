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
package com.bazaarvoice.jolt.convertr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ArrayKey extends Key {

    private Collection<Integer> keyInts;
    private int keyInt = -1;

    public ArrayKey( String jsonKey, Object spec ) {
        super( jsonKey, spec );

        // Handle ArrayKey specific stuff
        switch( getOp() ){
            case OR :
                keyInts = new ArrayList<>();
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
    protected int getLiteralIntKey() {
        return keyInt;
    }

    @Override
    protected void applyChild( Object container ) {

        if ( container instanceof List ) {
            @SuppressWarnings( "unchecked" )
            List<Object> convertList = (List<Object>) container;

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( Integer literalKey : determineMatchingContainerKeys( convertList ) ) {
                applyLiteralKeyToContainer( literalKey, convertList );
            }
        }
        // Else there is disagreement (with respect to Array vs Map) between the data in
        //  the Container vs the Convertr Spec type for this key.  Container wins, so do nothing.
    }

    private void applyLiteralKeyToContainer( Integer literalIndex, List<Object> container ) {

        Object converteeValue = container.get( literalIndex );

        if ( children == null ) {
        	if ( converteeValue != null ) {
            	converteeValue = TYPE.convert(converteeValue, type);
                container.set( literalIndex, converteeValue );  // convert the type.
            }
        }
        else {
            // recurse by applying my children to this known valid container
            applyChildren( converteeValue );
        }
    }

    private Collection<Integer> determineMatchingContainerKeys( List<Object> container ) {

        switch ( getOp() ) {
            case LITERAL:
                // Container it should get these literal values added to it
                return keyInts;
            case STAR:
                // Identify all its keys
                // this assumes the container list has already been expanded to the right size
                List convertList = (List) container;
                List<Integer> allIndexes = new ArrayList<>( convertList.size() );
                for ( int index = 0; index < convertList.size(); index++ ) {
                    allIndexes.add( index );
                }

                return allIndexes;
            case OR:
                // Identify the intersection between the container "keys" and the OR values
                List<Integer> indexesInRange = new ArrayList<>();

                for ( Integer orValue : keyInts ) {
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
