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
package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

/**
 * For use on the LHS, allows the user to specify an explicit string to write out.
 * Aka given a input that is boolean, would want to write something out other than "true" / "false".
 */
public class HashPathElement extends BasePathElement implements MatchablePathElement {

    private final String keyValue;

    public HashPathElement( String key ) {
        super(key);

        if ( StringTools.isBlank( key ) ) {
            throw new SpecException( "HashPathElement cannot have empty String as input." );
        }

        if ( ! key.startsWith( "#" ) ) {
            throw new SpecException( "LHS # should start with a # : " + key );
        }

        if ( key.length() <= 1 ) {
            throw new SpecException( "HashPathElement input is too short : " + key );
        }


        if ( key.charAt( 1 ) == '(' ) {
            if ( key.charAt( key.length() -1 ) == ')' ) {
                keyValue = key.substring( 2, key.length() -1 );
            }
            else {
                throw new SpecException( "HashPathElement, mismatched parens : " + key );
            }
        }
        else {
            keyValue = key.substring( 1 );
        }
    }

    @Override
    public String getCanonicalForm() {
        return "#(" + keyValue + ")";
    }

    @Override
    public MatchedElement match( String dataKey, WalkedPath walkedPath ) {
        return new MatchedElement( keyValue );
    }
}
