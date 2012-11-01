package com.bazaarvoice.jolt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursively sorts all maps within a JSON object into sorted LinkedHashMaps so that serialized
 * representations are deterministic.  Useful for debugging and making test fixtures.
 *
 * The sort order is standard alphabetical ascending, with a special case for "~" prefixed keys to be bumped to the top.
 */
public class Sortr implements Chainable {

    @Override
    public Object process( Object input, Map<String, Object> operationEntry ) throws JoltException {
        Object spec = operationEntry.get( "spec" );
        if (spec == null) {
            throw new JoltException( "JOLT Sortr expected a spec in its operation entry, but instead got: " + operationEntry.toString() );
        }
        return sort( input );
    }

    public Object sort( Object input ) throws JoltException {
        return sortJson( input );
    }

    public static Object sortJson( Object obj ) {
        if ( obj instanceof Map ) {
            return sortMap( (Map<String, Object>) obj );
        } else if ( obj instanceof List ) {
            return ordered( (List<Object>) obj );
        } else {
            return obj;
        }
    }

    private static Map<String, Object> sortMap( Map<String, Object> map ) {
        List<String> keys = new ArrayList<String>( map.keySet() );
        Collections.sort( keys, jsonKeyComparator );

        LinkedHashMap<String,Object> orderedMap = new LinkedHashMap<String, Object>( map.size() );
        for ( String key : keys ) {
            orderedMap.put( key, sortJson( map.get(key) ) );
        }
        return orderedMap;
    }

    private static List<Object> ordered( List<Object> list ) {
        // don't sort the list because that would change intent, but sort its components
        for ( int index = 0; index < list.size(); index++ ) {
            Object obj = list.get( index );
            list.set( index, sortJson( obj ) );
        }
        return list;
    }

    private static JsonKeyComparator jsonKeyComparator = new JsonKeyComparator();

    /**
     * Standard alphabetical sort, with a special case for keys beginning with "~".
     */
    private static class JsonKeyComparator implements Comparator<String> {

        @Override
        public int compare(String a, String b) {

            boolean aTilde = ( a.charAt(0) == '~' );
            boolean bTilde = ( b.charAt(0) == '~' );

            if ( aTilde && ! bTilde ) {
                return -1;
            }
            if ( ! aTilde && bTilde ) {
                return 1;
            }

            return a.compareTo( b );
        }
    }
}
