package com.bazaarvoice.jolt.shiftr;

import java.util.ArrayList;
import java.util.List;

import com.bazaarvoice.jolt.traversr.Traversr;
import com.bazaarvoice.jolt.traversr.traversal.Traversal;
import com.bazaarvoice.jolt.traversr.traversal.Traversal.Operation;

public class ShiftrTraversr extends Traversr {

    public ShiftrTraversr( String humanPath ) {
        super( humanPath );
    }

    /**
     * Do a Shift style insert :
     *  1) if there is no data "there", then just set it
     *  2) if there is already a list "there", just add the data to the list
     *  3) if there something other than a list there, grab it and stuff it and the data into a list
     *     and overwrite what is there with a list.
     */
    public Object handleFinalSet( Traversal traversal, Object tree, String key, Object data ) {

        Object sub = traversal.get( tree, key );

        if ( sub == null ) {
            // nothing is here so just set the data
            traversal.overwriteSet( tree, key, data );
        }
        else if ( sub instanceof List ) {
            // there is a list here, so we just add to it
            ((List<Object>) sub).add( data );
        }
        else {
            // take whatever is there and make it the first element in an Array
            List<Object> temp = new ArrayList<Object>();
            temp.add( sub );
            temp.add( data );

            traversal.overwriteSet( tree, key, temp );
        }

        return data;
    }


    /**
     * Shiftr style intermediate step.
     *
     * Only make a new instance of a container object for SET, if there is nothing "there".
     */
    @Override
    public Object handleIntermediateGet( Traversal traversal, Object tree,
                                         String key, Operation op ) {

        Object sub = traversal.get( tree, key );

        if ( sub == null && op == Operation.SET ) {

            // get our child to make the container object, so it will be happy with it
            sub = traversal.getChild().newContainer();
            traversal.overwriteSet( tree, key, sub );
        }

        return sub;
    }
}
