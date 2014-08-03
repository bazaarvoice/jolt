/*
 * Copyright 2014 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.common.PathStep;
import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.PathEvaluatingTraversal;
import com.bazaarvoice.jolt.shiftr.TransposeReader;
import com.bazaarvoice.jolt.utils.StringTools;

/**
 * This PathElement is used by Shiftr to Transpose data.
 *
 * It can be used on the Left and Right hand sides of the spec.
 *
 * Input
 * {
 *   "author" : "Stephen Hawking",
 *   "book" : "A Brief History of Time"
 * }
 *
 * Wanted
 * {
 *   "Stephen Hawking" : "A Brief History of Time"
 * }
 *
 * The first part of the process is to allow a CompositeShiftr node to look down the input JSON tree.
 *
 * Spec
 * {
 *     "@author" : "@book"
 * }
 *
 *
 * Secondly, we can look up the tree, and come down a different path to locate data.
 *
 * For example of this see the following ShiftrUnit tests :
 *   LHS Lookup : json/shiftr/filterParents.json
 *   RHS Lookup : json/shiftr/transposeComplex6_rhs-complex-at.json
 *
 *
 * CanonicalForm Expansion
 *  Sugar
 *    "@2         -> "@(2,)
 *    "@(2)       -> "@(2,)
 *    "@author"   -> "@(0,author)"
 *    "@(author)" -> "@(0,author)"
 *
 *  Splenda
 *    "@(a.b)"    -> "@(0,a.b)"
 *    "@(a.&2.c)" -> "@(0,a.&(2,0).c)"
 */
public class TransposePathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final int upLevel;
    private final TransposeReader subPathReader;
    private final String canonicalForm;

    public static TransposePathElement parse( String key ) {

        if ( key == null || key.length() < 2 ) {
            throw new SpecException( "'Transpose Input' key '@', can not be null or of length 1.  Offending key : " + key );
        }
        if ( '@' != key.charAt( 0 ) ) {
            throw new SpecException( "'Transpose Input' key must start with an '@'.  Offending key : " + key );
        }

        // Strip off the leading '@' as we don't need it anymore.
        String path = key.substring( 1 );

        if ( path.contains( "@" ) ) {
            throw new SpecException( "@ pathElement can not contain a nested @." );
        }
        if ( path.contains( "*" ) || path.contains( "[]" ) ) {
            throw new SpecException( "'Transpose Input' can not contain expansion wildcards (* and []).  Offending key : " + key );
        }

        // Check to see if the key is wrapped by parens
        if ( path.startsWith( "(" ) ) {
            if ( path.endsWith( ")" ) ) {
                path = path.substring( 1, path.length() - 1 );
            }
            else {
                throw new SpecException( "@ path element that starts with '(' must have a matching ')'.  Offending key : " + key );
            }
        }

        return parse2( key, path );
    }

    private static TransposePathElement parse2( String originalKey, String meat ) {

        char first = meat.charAt( 0 );
        if ( Character.isDigit( first ) ) {
            // loop until we find a comma or end of string
            StringBuilder sb = new StringBuilder().append( first );
            for ( int index = 1; index < meat.length(); index++ ) {
                char c = meat.charAt( index );

                // when we find a / the first comma, stop looking for integers, and just assume the rest is a String path
                if( ',' == c ) {

                    int upLevel;
                    try {
                        upLevel = Integer.valueOf( sb.toString() );
                    }
                    catch ( NumberFormatException nfe ) {
                        // I don't know how this exception would get thrown, as all the chars were checked by isDigit, but oh well
                        throw new SpecException( "@ path element with non/mixed numeric key is not valid, key=" + originalKey );
                    }

                    return new TransposePathElement( originalKey, upLevel, meat.substring( index + 1 ) );
                }
                else if ( Character.isDigit( c ) ) {
                    sb.append( c );
                }
                else {
                    throw new SpecException( "@ path element with non/mixed numeric key is not valid, key=" + originalKey );
                }
            }

            // if we got out of the for loop, then the whole thing was a number.
            return new TransposePathElement( originalKey, Integer.valueOf( sb.toString() ), null );
        }
        else {
            return new TransposePathElement( originalKey, 0, meat );
        }
    }

    private TransposePathElement( String originalKey, int upLevel, String subPath ) {
        super(originalKey);
        this.upLevel = upLevel;
        if ( StringTools.isEmpty( subPath ) ) {
            this.subPathReader = null;
            canonicalForm = "@(" + upLevel + ",)";
        }
        else {
            subPathReader = new TransposeReader(subPath);
            canonicalForm = "@(" + upLevel + "," + subPathReader.getCanonicalForm() + ")";
        }
    }

    public Object rawEval( WalkedPath walkedPath ) {
        // Grap the data we need from however far up the tree we are supposed to go
        PathStep pathStep = walkedPath.elementFromEnd( upLevel );

        Object treeRef = pathStep.getTreeRef();

        // Now walk down from that day using the subPathReader
        if ( subPathReader == null ) {
            return treeRef;
        }
        else {
            Object data = subPathReader.read( treeRef, walkedPath );
            return data;
        }
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {

        Object dataFromTranspose = rawEval( walkedPath );

        if ( dataFromTranspose instanceof Number ) {
            // the idea here being we are looking for an array index value
            int val = ((Number) dataFromTranspose).intValue();
            return Integer.toString( val );
        }

        if ( dataFromTranspose == null || ! ( dataFromTranspose instanceof String ) ) {

            // If this output path has a TransposePathElement, and when we evaluate it
            //  it does not resolve to a String, then return null
            return null;
        }

        return (String) dataFromTranspose;
    }


    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        return walkedPath.lastElement().getLiteralPathElement();  // copy what our parent was so that write keys of &0 and &1 both work.
    }

    @Override
    public String getCanonicalForm() {
        return canonicalForm;
    }
}
