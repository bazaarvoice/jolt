package com.bazaarvoice.jolt.shiftr.reference;

import com.bazaarvoice.jolt.exception.SpecException;

public abstract class BasePathReference implements PathReference {

    private final int pathIndex;    // equals 0 for "&"  "&0"  and  "&(0,x)"

    protected abstract char getToken();

    public BasePathReference( String refStr ) {

        if ( refStr == null || refStr.length() == 0 || getToken() != refStr.charAt( 0 ) ) {
            throw new SpecException( "Invalid reference key=" + refStr + " either blank or doesn't start with correct character=" + getToken() );
        }

        int pathIndex = 0;

        try {
            if ( refStr.length() > 1 ) {

                String meat = refStr.substring( 1 );

                pathIndex = Integer.parseInt( meat );
            }
        }
        catch( NumberFormatException nfe ) {
            throw new SpecException( "Unable to parse '" + getToken() + "' reference key:" + refStr, nfe );
        }

        if ( pathIndex < 0 ) {
            throw new SpecException( "Reference:" + refStr + " can not have a negative value."  );
        }

        this.pathIndex = pathIndex;
    }

    @Override
    public int getPathIndex() {
        return pathIndex;
    }

    /**
     * Builds the non-syntactic sugar / maximally expanded and unique form of this reference.
     * @return canonical form : aka "#" -> "#0
     */
    public String getCanonicalForm() {
        return getToken() + Integer.toString( pathIndex );
    }
}
