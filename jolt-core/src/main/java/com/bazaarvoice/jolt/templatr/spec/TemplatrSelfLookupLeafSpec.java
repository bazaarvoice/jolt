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

package com.bazaarvoice.jolt.templatr.spec;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.PathEvaluatingTraversal;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.pathelement.TransposePathElement;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.OpMode;

import java.util.Map;

/**
 * This spec helps lookup a specific leaf value from input and copies that to a different leaf node in input
 */
public class TemplatrSelfLookupLeafSpec extends TemplatrSpec {

    private final TransposePathElement rhsPathElement;

    public TemplatrSelfLookupLeafSpec( final String rawJsonKey, final String rhsValue, final OpMode opMode ) {
        super( rawJsonKey, opMode );

        PathEvaluatingTraversal templatrReader = TRAVERSAL_BUILDER.build( rhsValue );
        PathElement element = templatrReader.get( templatrReader.size() - 1 );
        if(element instanceof TransposePathElement) {
            rhsPathElement = (TransposePathElement) element;
        }
        else {
            throw new SpecException( "Expected @ path element here" );
        }
    }

    @Override
    public boolean applyElement( final String inputKey, final Object input, final MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context ) {

        Object parent = walkedPath.lastElement().getTreeRef();

        walkedPath.add( input, thisLevel );
        Optional<Object> objectOptional = rhsPathElement.objectEvaluate( walkedPath );
        walkedPath.removeLast();

        if(objectOptional.isPresent()) {
            setData( parent, thisLevel, objectOptional.get(), opMode );
        }

        return true;
    }
}
