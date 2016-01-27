package com.bazaarvoice.jolt.convertr;

import java.util.Collection;
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
            @SuppressWarnings("unchecked")
			Map<String, Object> converteeMap = (Map<String, Object>) container;

            // Find all convertee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( String literalKey : determineMatchingContainerKeys( converteeMap ) ) {
                applyLiteralKeyToContainer( literalKey, converteeMap );
            }
        }
        // Else there is disagreement (with respect to Array vs Map) between the data in
        //  the Container vs the Convertr Spec type for this key.  Container wins, so do nothing.
    }

    private void applyLiteralKeyToContainer( String literalKey, Map<String, Object> container ) {
        Object converteeValue = container.get( literalKey );

        if ( children == null ) {
            if ( converteeValue != null ) {
            	converteeValue = TYPE.convert(converteeValue, type);
                container.put( literalKey, converteeValue );  // convert type.
            }
        }
        else {
            // recurse by applying my children to this known valid container
            applyChildren( converteeValue );
        }
    }

    private Collection<String> determineMatchingContainerKeys( Map<String, Object> container ) {

        switch ( getOp() ) {
            case LITERAL:
                // the container should get these literal values added to it
                return keyStrings;
            case STAR:
                // Identify all its keys
                return container.keySet();
            case OR:
                // Identify the intersection between its keys and the OR values
                Set<String> intersection = new HashSet<>( container.keySet() );
                intersection.retainAll( keyStrings );
                return intersection;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }
}
