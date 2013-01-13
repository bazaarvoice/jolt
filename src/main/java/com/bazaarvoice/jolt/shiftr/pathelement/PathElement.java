package com.bazaarvoice.jolt.shiftr.pathelement;

public interface PathElement {

    String getRawKey();

    /**
     * Get the canonical form of this PathElement.  Really only interesting for the Reference Path element, where
     *  it will expand "&" to "&0(0)".
     * @return canonical String version of this PathElement
     */
    String getCanonicalForm();
}
