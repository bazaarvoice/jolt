package com.bazaarvoice.jolt.common.pathelement;

/**
 * Marker interface for PathElements that contain the "*" wildcard.
 *
 * Three subclasses were created for performance reasons.
 */
public interface StarPathElement extends MatchablePathElement {

    /**
     * Method to see if a candidate key would match this PathElement.
     *
     * @return true if the provided literal will match this Element's regex
     */
    public boolean stringMatch( String literal );
}
