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

/**
 * Meant to be an immutable PathElement from a Spec, and therefore shareable across
 *  threads running multiple transforms using the same spec.
 */
public class LiteralPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final String canonicalForm;

    public LiteralPathElement( String key ) {
        super(key);

        this.canonicalForm = key.replace( ".", "\\." );
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {
        return getRawKey();
    }

    @Override
    public MatchedElement match( String dataKey, WalkedPath walkedPath ) {
        if ( getRawKey().equals( dataKey ) ) {
            return new MatchedElement( getRawKey() );
        }
        return null;
    }

    @Override
    public String getCanonicalForm() {
        return canonicalForm;
    }
}
