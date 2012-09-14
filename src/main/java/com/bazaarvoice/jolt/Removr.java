package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.defaultr.Key;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class Removr implements Chainable {

    /**
     */
    @Override
    public Object process( Object input, Map<String, Object> operationEntry ) throws JoltException {
        Object spec = operationEntry.get( "spec" );
        if (spec == null) {
            throw new JoltException( "JOLT Removr expected a spec in its operation entry, but instead got: " + operationEntry.toString() );
        }
        try {
            return removr( spec, input);
        }
        catch( Exception e) {
            throw new JoltException( e );
        }
    }

    /**
     */
    public Object removr( Object specObj, Object removeeObj ) {

        if ( specObj != null && removeeObj != null && specObj instanceof Map && removeeObj instanceof Map ) {
            Map<String,Object> spec = (Map<String,Object>) specObj;
            Map<String,Object> removee = (Map<String,Object>) removeeObj;

            for ( String nukeKey : spec.keySet() ) {

                Object subNuke = spec.get( nukeKey );
                if ( subNuke != null && subNuke instanceof Map ) {
                    removr( subNuke, removee.get( nukeKey ) );
                }
                else {
                    removee.remove( nukeKey );
                }
            }
        }
        return removeeObj;
    }
}