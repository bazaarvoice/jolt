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

/**
 * All "References" extend this class and support three level of syntactic sugar
 * Example with the AmpReference
 *  1   "&"
 *  2   "&0"
 *  3   "&(0,0)"
 *  all three mean the same thing.
 *
 *  References are used to look up values in a WalkedPath.
 *  In the CanonicalForm the first entry is how far up the WalkedPath to look for a LiteralPathElement,
 *   and the second entry is which part of that LiteralPathElement to ask for.
 */
public abstract class BasePathAndGroupReference implements PathAndGroupReference {

    private final int keyGroup;     // equals 0 for "&"  "&0"  and  "&(x,0)"
    private final int pathIndex;    // equals 0 for "&"  "&0"  and  "&(0,x)"

    protected abstract char getToken();

    public BasePathAndGroupReference( String refStr ) {

        if ( refStr == null || refStr.length() == 0 || getToken() != refStr.charAt( 0 ) ) {
            throw new SpecException( "Invalid reference key=" + refStr + " either blank or doesn't start with correct character=" + getToken() );
        }

        int pI = 0;
        int kG = 0;

        try {
            if ( refStr.length() > 1 ) {

                String meat = refStr.substring( 1 );

                if( meat.length() >= 3 && meat.startsWith( "(" ) && meat.endsWith( ")" ) ) {

                    // "&(1,2)" -> "1,2".split( "," ) -> String[] { "1", "2" }    OR
                    // "&(3)"   -> "3".split( "," ) -> String[] { "3" }

                    String parenMeat = meat.substring( 1, meat.length() -1 );
                    String[] intStrs = parenMeat.split( "," );
                    if ( intStrs.length > 2 ) {
                        throw new SpecException( "Invalid Reference=" + refStr );
                    }

                    pI = Integer.parseInt( intStrs[0] );
                    if ( intStrs.length == 2 ) {
                        kG = Integer.parseInt( intStrs[1] );
                    }
                }
                else {   // &2
                    pI = Integer.parseInt( meat );
                }
            }
        }
        catch( NumberFormatException nfe ) {
            throw new SpecException( "Unable to parse '" + getToken() + "' reference key:" + refStr, nfe );
        }

        if ( pI < 0 || kG < 0 ) {
            throw new SpecException( "Reference:" + refStr + " can not have a negative value."  );
        }

        pathIndex = pI;
        keyGroup = kG;
    }

    public int getPathIndex() {
        return pathIndex;
    }

    public int getKeyGroup() {
        return keyGroup;
    }

    /**
     * Builds the non-syntactic sugar / maximally expanded and unique form of this reference.
     * @return canonical form : aka "&" -> "&(0,0)
     */
    public String getCanonicalForm() {
        return getToken() + "(" + pathIndex + "," + keyGroup + ")";
    }
}
