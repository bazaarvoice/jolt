package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.shiftr.pathelement.LiteralPathElement;

import java.util.ArrayList;
import java.util.Collection;

/**
 * DataStructure used by Shiftr during it's parallel tree walk.
 *
 * At any given point in time, it represents where in the tree walk a Shiftr Spec is operating.
 * It is primarily used to by the LeafSpec as a reference to lookup real values for output "&(1,1)"
 *  references.
 *
 * It is expected that as Shiftr navigates down the tree, LiteralElements will be added and then
 *  removed when that subtree has been walked.
 */
public class WalkedPath extends ArrayList<LiteralPathElement> {

    public WalkedPath() {
        super();
    }

    public WalkedPath( Collection<LiteralPathElement> c ) {
        super( c );
    }

    public LiteralPathElement removeLast() {
        return remove( size() - 1 );
    }

    /**
     * Method useful to "&", "&1", "&2", etc evaluation.
     */
    public LiteralPathElement elementFromEnd( int idxFromEnd ) {
        if ( isEmpty() ) {
            return null;
        }
        return get( size() - 1 - idxFromEnd );
    }

    public LiteralPathElement lastElement() {
        return get( size() - 1 );
    }

}
