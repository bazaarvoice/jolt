package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.traversr.Traversr;
import com.bazaarvoice.jolt.traversr.TraversrException;

import java.util.List;

/**
 * Subclass of ArrayTraversalStep that does not care about array index numbers.
 * Instead it will just do an array add on any set.
 *
 * Consequently, get and remove are rather meaningless.
 *
 * This exists, because we need a way in the human readable path, so say that we
 *  always want a list value.
 *
 * Example : "tuna.marlin.[]"
 *   We want the value of marlin to always be a list, and anytime we set data
 *   to marlin, it should just be added to the list.
 */
public class AutoExpandArrayTraversalStep extends ArrayTraversalStep {

    public AutoExpandArrayTraversalStep( Traversr traversr, TraversalStep child ) {
        super( traversr, child );
    }

    @Override
    public Object get( List<Object> list, String key ) {

        if ( ! "[]".equals( key ) ) {
            throw new TraversrException( "AutoExpandArrayTraversal expects a '[]' key." );
        }

        return null;
    }

    @Override
    public Object remove( List<Object> list, String key ) {

        if ( ! "[]".equals( key ) ) {
            throw new TraversrException( "AutoExpandArrayTraversal expects a '[]' key." );
        }

        return null;
    }

    @Override
    public Object overwriteSet( List<Object> list, String key, Object data ) {

        if ( ! "[]".equals( key ) ) {
            throw new TraversrException( "AutoExpandArrayTraversal expects a '[]' key." );
        }

        list.add( data );
        return data;
    }
}
