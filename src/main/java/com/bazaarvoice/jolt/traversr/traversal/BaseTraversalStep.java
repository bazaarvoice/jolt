package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.traversr.Traversr;
import com.bazaarvoice.jolt.traversr.TraversrException;

import java.util.Iterator;


public abstract class BaseTraversalStep<T> implements TraversalStep<T> {

    protected final TraversalStep child;
    protected final Traversr traversr;

    public BaseTraversalStep( Traversr traversr, TraversalStep child ) {
        this.traversr = traversr;
        this.child = child;
    }

    public TraversalStep getChild() {
        return child;
    }

    public final Object traverse( Object tree, Operation op, Iterator<String> keys, Object data ) {

        if ( tree == null ) {
            return null;
        }

        if ( getStepType().isAssignableFrom( tree.getClass() ) ) {

            String key = keys.next();

            if ( child == null ) {
                // End of the Traversal so do the set or get
                switch (op) {
                    case GET :
                        return this.get( (T) tree, key );
                    case SET :
                        return traversr.handleFinalSet( this, tree, key, data );
                    case REMOVE:
                        return this.remove( (T) tree, key );
                    default :
                        throw new IllegalStateException( "Invalid op:" + op.toString() );
                }
            }
            else {

                // We just an intermediate step, so traverse and then hand over control to our child
                Object sub = traversr.handleIntermediateGet( this, tree, key, op );

                return child.traverse( sub, op, keys, data );
            }
        }
        else {
            // TODO make the throwing of this Exception be something handled / optional by the travesr
            //  Aka combine the typeOk logic with this
            //  If type != ok options are :
            //    Do nothing
            //    Throw Exception
            //    "make" the type ok by overwriting with a new container object (got list, wanted map, so trash the list by overwriting with a new map)
            throw new TraversrException( "Type mismatch on parent." );
        }
    }
}
