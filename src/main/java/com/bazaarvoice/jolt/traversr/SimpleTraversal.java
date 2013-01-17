package com.bazaarvoice.jolt.traversr;

import java.util.Arrays;
import java.util.List;

/**
 * Utility class for use in custom Transforms.
 *
 * Allows a programmer to just provide a single "human readable path"
 *  that they will want to be able to execute against multiple trees of data.
 *
 * Internally, parses the "human readable path" into a Traversr and a set of keys,
 *  so that the user only needs to call get/set with their input tree.
 *
 * Because the path is static, it is assumed that you will always be reading and writing
 *  objects of the same type to the tree, therefore this class can take a generic
 *  parameter "K" to reduce casting.
 */
public class SimpleTraversal<K> {

    private final SimpleTraversr traversr;
    private final List<String> keys;

    /**
     * Google Maps.newHashMap() trick to fill in generic type
     */
    public static <T> SimpleTraversal<T> newTraversal(String humanReadablePath) {
        return new SimpleTraversal<T>( humanReadablePath );
    }

    public SimpleTraversal( String humanReadablePath ) {
        traversr = new SimpleTraversr( humanReadablePath );

        String[] keysArray = humanReadablePath.split( "\\." );

        // extract the 3 from "[3]", but don't mess with "[]"
        for ( int index = 0; index < keysArray.length; index++) {

            String key = keysArray[ index ];
            if ( key.charAt( 0 ) == '[' && key.charAt( key.length() -1 ) == ']' ) {
                if ( key.length() > 2 ) {
                    keysArray[index] = key.substring( 1, key.length() - 1 );
                }
            }
        }

        keys = Arrays.asList( keysArray );
    }

    /**
     * @param tree tree of Map and List JSON structure to navigate
     * @return the object you wanted, or null if the object or any step along the path to it were not there
     */
    public K get( Object tree ) {
        return (K) traversr.get( tree, keys );
    }

    /**
     * @param tree tree of Map and List JSON structure to navigate
     * @param data JSON style data object you want to set
     * @return returns the data object if successfully set, otherwise null if there was a problem walking the path
     */
    public K set( Object tree, K data ) {
        return (K) traversr.set( tree, keys, data );
    }
}
