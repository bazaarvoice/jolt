package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.traversr.Traversr;

import java.util.ArrayList;
import java.util.List;

public class ArrayTraversal extends BaseTraversal<List<Object>> {

    public ArrayTraversal( Traversr traversr, Traversal child ) {
        super( traversr, child );
    }

    public boolean typeOk( Object tree ) {
        return tree instanceof List;
    }

    public Object newContainer() {
        return new ArrayList<Object>();
    }

    @Override
    public Object doGet( List<Object> list, String key ) {

        int arrayIndex = Integer.parseInt( key );
        if ( arrayIndex < list.size() ) {
            return list.get( arrayIndex );
        }

        return null;
    }

    @Override
    public Object doOverwriteSet( List<Object> list, String key, Object data ) {

        int arrayIndex = Integer.parseInt( key );
        ensureArraySize( list, arrayIndex );            // make sure it is big enough
        list.set( arrayIndex, data );
        return data;
    }


    public static void ensureArraySize( List<Object> list, Integer upperIndex ) {
        for ( int sizing = list.size(); sizing <= upperIndex; sizing++ ) {
            list.add( null );
        }
    }
}
