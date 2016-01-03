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
package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.Traversr;

import java.util.Iterator;


public abstract class BaseTraversalStep<StepType,DataType> implements TraversalStep<StepType,DataType> {

    protected final TraversalStep child;
    protected final Traversr traversr;

    public BaseTraversalStep( Traversr traversr, TraversalStep child ) {
        this.traversr = traversr;
        this.child = child;
    }

    public TraversalStep getChild() {
        return child;
    }

    public final Optional<DataType> traverse( StepType tree, Operation op, Iterator<String> keys, DataType data ) {

        if ( tree == null ) {
            return Optional.empty();
        }

        if ( getStepType().isAssignableFrom( tree.getClass() ) ) {

            String key = keys.next();

            if ( child == null ) {
                // End of the Traversal so do the set or get
                switch (op) {
                    case GET :
                        return this.get( tree, key );
                    case SET :
                        return (Optional<DataType>) traversr.handleFinalSet( this, tree, key, data );
                    case REMOVE:
                        return this.remove( tree, key );
                    default :
                        throw new IllegalStateException( "Invalid op:" + op.toString() );
                }
            }
            else {

                // We just an intermediate step, so traverse and then hand over control to our child
                Optional<Object> optSub = traversr.handleIntermediateGet( this, tree, key, op );

                if ( optSub.isPresent() ) {
                    return child.traverse( optSub.get(), op, keys, data );
                }
            }
        }

        return Optional.empty();
    }
}
