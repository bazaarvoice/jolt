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
import com.bazaarvoice.jolt.common.TraversalBuilder;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.common.OpMode;

import java.util.Map;

/**
 * This spec helps lookup a specific leaf value from a given context object and copies that to a leaf node in input
 */
public class TemplatrContextLookupLeafSpec extends TemplatrSpec {

    private final PathEvaluatingTraversal templatrReader;

    public TemplatrContextLookupLeafSpec( final String rawJsonKey, final String rhsValue, final OpMode opMode ) {
        super( rawJsonKey, opMode );
        templatrReader = TRAVERSAL_BUILDER.build( rhsValue.substring( 1 ) );
    }

    @Override
    public boolean applyElement( final String inputKey, final Object input, final MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context ) {

        Object parent = walkedPath.lastElement().getTreeRef();
        walkedPath.add( input, thisLevel );

        Optional<Object> dataOptional = templatrReader.read( context, walkedPath );
        if(dataOptional.isPresent()) {
            setData( parent, thisLevel, dataOptional.get(), opMode );
        }
        walkedPath.removeLast();

        return true;
    }
}
