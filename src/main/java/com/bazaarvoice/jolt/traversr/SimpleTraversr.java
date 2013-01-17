package com.bazaarvoice.jolt.traversr;

import com.bazaarvoice.jolt.traversr.traversal.TraversalStep;

/**
 * Simple Traversr that
 *
 * 1 Does overwrite sets at the leaf level
 * 2 Will create intermediate container objects only on SET operations
 */
public class SimpleTraversr extends Traversr {

    public SimpleTraversr( String humanPath ) {
        super( humanPath );
    }

    @Override
    public Object handleFinalSet( TraversalStep traversalStep, Object tree, String key, Object data ) {
        return traversalStep.overwriteSet( tree, key, data );
    }

    /**
     * Only make a new instance of a container object for SET, if there is nothing "there".
     */
    @Override
    public Object handleIntermediateGet( TraversalStep traversalStep, Object tree, String key, TraversalStep.Operation op ) {

        Object sub = traversalStep.get( tree, key );

        if ( sub == null && op == TraversalStep.Operation.SET ) {

            // get our child to make the container object, so it will be happy with it
            sub = traversalStep.getChild().newContainer();
            traversalStep.overwriteSet( tree, key, sub );
        }

        return sub;
    }
}