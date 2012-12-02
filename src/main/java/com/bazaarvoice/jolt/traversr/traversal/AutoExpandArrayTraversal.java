package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.traversr.Traversr;
import com.bazaarvoice.jolt.traversr.TraversrException;

import java.util.List;

public class AutoExpandArrayTraversal extends ArrayTraversal {

    public AutoExpandArrayTraversal(Traversr traversr, Traversal child ) {
        super( traversr, child );
    }

    @Override
    public Object doGet( List<Object> list, String key ) {

        if ( ! "[]".equals( key ) ) {
            throw new TraversrException( "AutoExpandArrayTraversal expects a '[]' key." );
        }

        return null;
    }

    @Override
    public Object doOverwriteSet( List<Object> list, String key, Object data ) {

        if ( ! "[]".equals( key ) ) {
            throw new TraversrException( "AutoExpandArrayTraversal expects a '[]' key." );
        }

        list.add( data );
        return data;
    }
}
