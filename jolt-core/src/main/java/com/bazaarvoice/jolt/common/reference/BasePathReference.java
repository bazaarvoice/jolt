/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.common.reference;

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
