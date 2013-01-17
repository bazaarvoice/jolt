package com.bazaarvoice.jolt.shiftr;

import java.util.ArrayList;
import java.util.List;

import com.bazaarvoice.jolt.traversr.SimpleTraversr;
import com.bazaarvoice.jolt.traversr.traversal.TraversalStep;

public class ShiftrTraversr extends SimpleTraversr {

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
    public Object handleFinalSet( TraversalStep traversalStep, Object tree, String key, Object data ) {

        Object sub = traversalStep.get( tree, key );

        if ( sub == null ) {
            // nothing is here so just set the data
            traversalStep.overwriteSet( tree, key, data );
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

            traversalStep.overwriteSet( tree, key, temp );
        }

        return data;
    }
}
