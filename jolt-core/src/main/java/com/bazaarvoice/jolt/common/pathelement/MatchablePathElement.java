package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.common.WalkedPath;

public interface MatchablePathElement extends PathElement {

    /**
     * See if this PathElement matches the given dataKey.  If it does not match, this method returns null.
     *
     * If this PathElement does match, it returns a LiteralPathElement with subKeys filled in.
     *
     * @param dataKey String key value from the input data
     * @param walkedPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing its match
     * @return null or a matched LiteralPathElement
     */
    LiteralPathElement match( String dataKey, WalkedPath walkedPath );
}
