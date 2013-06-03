package com.bazaarvoice.jolt.common.reference;


/**
 * Reference is used by Shiftr when lookup up values from a WalkedPath (list of LiteralPathElements).
 *
 *   #,  #0  are the same
 *
 * The "canonical form" is "Cx", where :
 *  C : the character used to determine the type of Reference
 *  x : pathIndex : which is how far up the walkedPath the look
 *
 */
public interface PathReference {

    public int getPathIndex();

    /**
     * Get the canonical form of this Reference.
     *
     * One of the uses of this method is to ensure that spec, does not contain "duplicate" keys, aka
     *  two keys that when you unroll the syntactic sugar, are the same thing.
     *
     * @return fully expanded String representation of this Reference
     */
    public String getCanonicalForm();
}
