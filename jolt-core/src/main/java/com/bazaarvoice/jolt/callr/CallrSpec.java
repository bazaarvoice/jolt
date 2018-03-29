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
package com.bazaarvoice.jolt.callr;

import com.bazaarvoice.jolt.common.pathelement.*;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.Map;

public abstract class CallrSpec {

    protected static final String CLASS = "~class";
    protected static final String METHOD = "~method";
    protected static final String ARGS = "~args";

    private static final String STAR = "*";
    private static final String AT = "@";

    protected final MatchablePathElement pathElement;

    public CallrSpec( String rawJsonKey ) {
        PathElement pe = parse( rawJsonKey );
        if ( !( pe instanceof MatchablePathElement ) ) {
            throw new SpecException( "Spec LHS key=" + rawJsonKey + " is not a valid LHS key." );
        }
        this.pathElement = (MatchablePathElement) pe;
    }

    public static PathElement parse( String key ) {

        if ( key.contains( AT ) ) {
            return new AtPathElement( key );
        } else if ( STAR.equals( key ) ) {
            return new StarAllPathElement( key );
        } else if ( key.contains( STAR ) ) {
            if ( StringTools.countMatches( key, STAR ) == 1 ) {
                return new StarSinglePathElement( key );
            } else {
                return new StarRegexPathElement( key );
            }
        } else {
            return new LiteralPathElement( key );
        }
    }

    public boolean isLeaf( Map<String, Object> rhs ) {
        if ( !rhs.containsKey( CLASS ) || !rhs.containsKey( METHOD ) || !rhs.containsKey( ARGS ) ) {
            return false;
        }
        return true;
    }

    public abstract boolean apply( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer );
}
