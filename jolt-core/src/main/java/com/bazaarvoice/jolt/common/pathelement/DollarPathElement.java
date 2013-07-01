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

import com.bazaarvoice.jolt.common.reference.DollarReference;
import com.bazaarvoice.jolt.common.WalkedPath;

public class DollarPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final DollarReference dRef;

    public DollarPathElement( String key ) {
        super(key);

        dRef = new DollarReference( key );
    }

    @Override
    public String getCanonicalForm() {
        return dRef.getCanonicalForm();
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {
        LiteralPathElement pe = walkedPath.elementFromEnd( dRef.getPathIndex() );
        return pe.getSubKeyRef( dRef.getKeyGroup() );
    }

    @Override
    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        String evaled = evaluate( walkedPath );
        return new LiteralPathElement( evaled );
    }
}
