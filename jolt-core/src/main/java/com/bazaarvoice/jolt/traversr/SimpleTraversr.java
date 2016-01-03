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
package com.bazaarvoice.jolt.traversr;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.traversal.TraversalStep;

import java.util.List;

/**
 * Simple Traversr that
 *
 * 1 Does overwrite sets at the leaf level
 * 2 Will create intermediate container objects only on SET operations
 */
public class SimpleTraversr<DataType> extends Traversr<DataType> {

    public SimpleTraversr( String humanPath ) {
        super( humanPath );
    }

    public SimpleTraversr( List<String> paths ) {
        super( paths );
    }

    @Override
    public Optional<DataType> handleFinalSet( TraversalStep traversalStep, Object tree, String key, DataType data ) {
        return traversalStep.overwriteSet( tree, key, data );
    }

    /**
     * Only make a new instance of a container object for SET, if there is nothing "there".
     */
    @Override
    public Optional<DataType> handleIntermediateGet( TraversalStep traversalStep, Object tree, String key, TraversalStep.Operation op ) {

        Optional<Object> optSub = traversalStep.get( tree, key );

        Object sub = optSub.get();

        if ( sub == null && op == TraversalStep.Operation.SET ) {

            // get our child to make the container object, so it will be happy with it
            sub = traversalStep.getChild().newContainer();
            traversalStep.overwriteSet( tree, key, sub );
        }

        return Optional.of( (DataType) sub );
    }
}
