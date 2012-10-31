package com.bazaarvoice.jolt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Recursively sorts all maps within a JSON object to sorted LinkedHashMaps so that the toString()
 * representations are deterministic.  Useful for debugging and making test fixtures.
 *
 * The sort or is standard alphabetical, with a special case for "~" prefixed keys to be sorted to the top.
 */
public class Sortr implements Chainable {

    @Override
    public Object process( Object input, Map<String, Object> operationEntry ) throws JoltException {
        return sortJson( input );
    }

    public Object sort( Object input, Object operationEntry ) throws JoltException {
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
        // don't sort the list because that would change intent.  only maps and sets get sorted.
        for ( Object value : list ) {
            sortJson( value );
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
