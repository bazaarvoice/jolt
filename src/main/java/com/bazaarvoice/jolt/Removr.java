package com.bazaarvoice.jolt;

import java.util.Map;

/**
 * Removr is a kind of JOLT transform that removes content from the input Json.
 * <p/>
 * For comparision :
 * Shitr walks the input data and asks its spec "Where should this go?"
 * Defaultr walks the spec and asks "Does this exist in the data?  If not, add it."
 *
 * While, Removr walks the spec and asks "if this exists, remove it."
 * <p/>
 * Example : Given input Json like
 * <pre>
 * {
 *   "~emVersion" : "2",
 *   "id":"123124",
 *   "productId":"31231231",
 *   "submissionId":"34343",
 *   "this" : "stays",
 *   "configured" : {
 *     "a" : "b",
 *     "c" : "d"
 *   }
 * }
 * </pre>
 * With the desired output being :
 * <pre>
 * {
 *   "id":"123124",
 *   "this" : "stays",
 *
 *   "configured" : {
 *     "a" : "b"
 *   }
 * }
 * </pre>
 * This is what the Removr Spec would look like
 * <pre>
 * {
 *   "~emVersion" : "",
 *   "productId":"",
 *   "submissionId":"",
 *
 *   "configured" : {
 *     "c" : ""
 *   }
 * }
 * </pre>
 * <p/>
 * The Spec file format for Removr is a tree Map<String, Object> objects.
 * The "Right hand side" of the of each entry is ignored/irrelevant unless it is a map,
 *  in which case Removr will recursively walk down the tree.
 * <p/>
 */
public class Removr implements Chainable {

    /**
     * Chainable bindings for Removr.
     */
    @Override
    public Object process( Object input, Map<String, Object> operationEntry )
            throws JoltException {
        Object spec = operationEntry.get( "spec" );
        if ( spec == null ) {
            throw new JoltException( "JOLT Removr expected a spec in its operation entry, but instead got: " + operationEntry.toString() );
        }
        try {
            return removr( spec, input );
        } catch ( Exception e ) {
            throw new JoltException( e );
        }
    }

    /**
     * Recursively walk the spec and remove keys from the data.
     */
    public Object removr( Object specObj, Object removeeObj ) {

        if ( specObj != null && removeeObj != null && specObj instanceof Map && removeeObj instanceof Map ) {
            Map<String, Object> spec = (Map<String, Object>) specObj;
            Map<String, Object> removee = (Map<String, Object>) removeeObj;

            for ( String nukeKey : spec.keySet() ) {

                Object subNuke = spec.get( nukeKey );
                if ( subNuke != null && subNuke instanceof Map ) {
                    removr( subNuke, removee.get( nukeKey ) );
                } else {
                    removee.remove( nukeKey );
                }
            }
        }
        return removeeObj;
    }
}