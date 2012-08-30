package com.bazaarvoice.jolt.defaultr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapKey extends Key {

    public MapKey( String jsonKey, Object spec ) {
        super( jsonKey, spec );
    }

    @Override
    protected int getLiteralIntKey() {
        throw new UnsupportedOperationException( "Shouldn't be be asking a MapKey for int getLiteralIntKey()."  );
    }

    @Override
    protected void applyChild( Object container ) {

        if ( container instanceof Map ) {
            Map<String, Object> defaulteeMap = (Map<String, Object>) container;

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( String literalKey : determineMatchingContainerKeys( defaulteeMap ) ) {
                applyLiteralKeyToContainer( literalKey, defaulteeMap );
            }
        }
        // Else there is disagreement (with respect to Array vs Map) between the data in
        //  the Container vs the Defaultr Spec type for this key.  Container wins, so do nothing.
    }

    private void applyLiteralKeyToContainer( String literalKey, Map<String, Object> container ) {

        Object defaulteeValue = container.get( literalKey );

        if ( children == null ) {
            if ( defaulteeValue == null ) {
                container.put( literalKey, literalValue );  // apply a default value into a map
            }
        }
        else {
            if ( defaulteeValue == null ) {
                defaulteeValue = createOutputContainerObject();
                container.put( literalKey, defaulteeValue );  // push a new sub-container into this map
            }

            // recurse by applying my children to this known valid container
            applyChildren( defaulteeValue );
        }
    }

    private Collection<String> determineMatchingContainerKeys( Map<String, Object> container ) {

        switch ( getOp() ) {
            case LITERAL:
                // the container should get these literal values added to it
                return keyStrings;
            case STAR:
                // Identify all its keys
                return ( (Map) container ).keySet();
            case OR:
                // Identify the intersection between its keys and the OR values
                Set<String> intersection = new HashSet<String>( ( (Map) container ).keySet() );
                intersection.retainAll( keyStrings );
                return intersection;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }
}
