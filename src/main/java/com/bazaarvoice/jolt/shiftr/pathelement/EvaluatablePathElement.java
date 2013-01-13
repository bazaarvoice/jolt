package com.bazaarvoice.jolt.shiftr.pathelement;

import com.bazaarvoice.jolt.shiftr.WalkedPath;

public interface EvaluatablePathElement extends PathElement {

    /**
     * Evaluate this key as if it is an write path element.
     * @param walkedPath "up the tree" list of LiteralPathElements, that may be used by this key as it is computing
     * @return String path element to use for write tree building
     */
    String evaluate( WalkedPath walkedPath );
}
