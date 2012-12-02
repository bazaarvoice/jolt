package com.bazaarvoice.jolt.traversr.traversal;

import java.util.Iterator;

/**
 * A step in a JSON tree traversal.
 */
public interface Traversal {

    /**
     * The two things you can do with a Traversal.
     */
    public enum Operation { SET, GET }

    /**
     * Return the data for the key from the provided tree object.
     *
     * @return data object if available, or null.
     */
    public Object get( Object tree, String key );

    /**
     * Insert the data into the tree, overwriting any data that is there.
     *
     * @return returns the data object if successful or null if it could not
     */
    public Object overwriteSet( Object tree, String key, Object data );

    /**
     * @return the child Traversal or null if this Traversal has no child
     */
    public Traversal getChild();

    /**
     * Create a new mutable Map or List, suitable for this PathElement to traverse.
     *
     * @return new List or Map, depending on the type of the Traversal
     */
    public Object newContainer();

    /**
     * @return true if the supplied object is of the right type
     */
    public boolean typeOk( Object tree );

    /**
     * The meat of the Traversal.
     *
     * Pull a key from the iterator, use it to make the traversal, and then
     *  call traverse on your child Traversal.
     *
     * @param tree tree of data to walk
     * @param op the Operation to perform is this is the last node of the Traversal
     * @param keys keys to use
     * @param data the data to place if the operation is SET
     * @return if SET, null for fail or the "data" object for ok.  if GET, PANTS
     */
    public Object traverse( Object tree, Operation op, Iterator<String> keys, Object data );
}