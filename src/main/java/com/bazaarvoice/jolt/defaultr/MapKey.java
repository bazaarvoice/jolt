package com.bazaarvoice.jolt.defaultr;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapKey extends Key<String, Map<String,Object>> {

    public MapKey( String jsonKey ) {
        super.init( jsonKey );
    }

    public Collection<String> getKeyValues() {
        return Collections.unmodifiableCollection(keyStrings);
    }

    @Override
    public int getLiteralIntKey() {
        throw new IllegalStateException( "Shouldn't be be asking a MapKey for int getLiteralIntKey()."  );
    }

    @Override
    public void applySubSpec( Object subSpec, Object defaultee ) {

        if ( defaultee instanceof Map ) {

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( String literalKey : findMatchingDefaulteeKeys( defaultee ) ) {
                defaultLiteralValue( literalKey, subSpec, (Map<String, Object>) defaultee );
            }
        }
        // Else defaultee was not a container object, the wrong type of container object, or null
        //  net effect, we couldn't push values into it
    }

    @Override
    public void defaultLiteralValue( String literalKey, Object subSpec, Map<String, Object> defaultee ) {

        Object defaulteeValue = defaultee.get( literalKey );

        if ( subSpec instanceof Map ) {
            if ( defaulteeValue == null ) {
                defaulteeValue = createDefaultContainerObject();
                defaultee.put( literalKey, defaulteeValue );  // push a new sub-container into this map
            }

            // Re-curse into subspec
            applySpec( (Map<Key, Object>) subSpec, defaulteeValue );
        } else {
            if ( defaulteeValue == null ) {
                defaultee.put( literalKey, subSpec );  // apply a default value into a map
            }
        }
    }


    @Override
    public Collection<String> findMatchingDefaulteeKeys( Object defaultee ) {

        if ( defaultee == null ) {
            return Collections.emptyList();
        }

        switch ( getOp() ) {
            // If the Defaultee is not null, it should get these literal values added to it
            case LITERAL:
                return getKeyValues();
            // Identify all its keys
            case STAR:
                if ( defaultee instanceof Map ) {
                    return ( (Map) defaultee ).keySet();
                }
                break;
            // Identify the intersection between its keys and the OR values
            case OR:
                if ( defaultee instanceof Map ) {

                    Set<String> intersection = new HashSet<String>( ( (Map) defaultee ).keySet() );
                    intersection.retainAll( getKeyValues() );
                    return intersection;
                }
                break;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }

        return Collections.emptyList();
    }
}
