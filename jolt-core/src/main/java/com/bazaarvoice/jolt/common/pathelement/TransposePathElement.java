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

import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.shiftr.PathEvaluatingTraversal;
import com.bazaarvoice.jolt.shiftr.TransposeReader;

/**
 * This PathElement is used to Shiftr to Transpose data.
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
 * CanonicalForm
 *  "@author" -> "@(author)"
 *  "@(a.b)" -> "@(a.b)"
 *  "@(a.&2.c)" -> "@(a.&(2,0).c)"
 */
public class TransposePathElement extends BasePathElement implements MatchablePathElement {

    final String canonicalForm;
    final TransposeReader subPathReader;

    public TransposePathElement( String key ) {
        super(key);

        if ( key == null || key.length() < 2 ) {
            throw new SpecException( "'Transpose Input' key '@', can not be null or of length 1.  Offending key : " + key );
        }
        if ( '@' != key.charAt( 0 ) ) {
            throw new SpecException( "'Transpose Input' key must start with an '@'.  Offending key : " + key );
        }

        String path = key.substring( 1 );

        if ( path.contains( "*" ) || path.contains( "[]" ) ) {
            throw new SpecException( "'Transpose Input' can not contain wildcards.  Offending key : " + key );
        }

        if ( path.startsWith( "(" ) ) {
            if ( path.endsWith( ")" ) ) {
                path = path.substring( 1, path.length() - 1 );
            }
            else {
                throw new SpecException( "@ path element that starts with '(' must have a matching ')'." );
            }
        }

        if ( path.contains( "@" ) ) {
            throw new SpecException( "@ pathElement can not contain a nested @." );
        }

        subPathReader = new TransposeReader(path);
        canonicalForm = "@(" + subPathReader.getCanonicalForm() + ")";
    }



    public PathEvaluatingTraversal getSubPathReader() {
        return subPathReader;
    }

    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        return walkedPath.lastElement().getLiteralPathElement();  // copy what our parent was so that write keys of &0 and &1 both work.
    }

    @Override
    public String getCanonicalForm() {
        return canonicalForm;
    }
}
