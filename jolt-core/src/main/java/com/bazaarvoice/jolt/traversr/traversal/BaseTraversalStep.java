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

import com.bazaarvoice.jolt.traversr.Traversr;
import com.bazaarvoice.jolt.traversr.TraversrException;

import java.util.Iterator;


public abstract class BaseTraversalStep<T> implements TraversalStep<T> {

    protected final TraversalStep child;
    protected final Traversr traversr;

    public BaseTraversalStep( Traversr traversr, TraversalStep child ) {
        this.traversr = traversr;
        this.child = child;
    }

    public TraversalStep getChild() {
        return child;
    }

    public final Object traverse( Object tree, Operation op, Iterator<String> keys, Object data ) {

        if ( tree == null ) {
            return null;
        }

        if ( getStepType().isAssignableFrom( tree.getClass() ) ) {

            String key = keys.next();

            if ( child == null ) {
                // End of the Traversal so do the set or get
                switch (op) {
                    case GET :
                        return this.get( (T) tree, key );
                    case SET :
                        return traversr.handleFinalSet( this, tree, key, data );
                    case REMOVE:
                        return this.remove( (T) tree, key );
                    default :
                        throw new IllegalStateException( "Invalid op:" + op.toString() );
                }
            }
            else {

                // We just an intermediate step, so traverse and then hand over control to our child
                Object sub = traversr.handleIntermediateGet( this, tree, key, op );

                return child.traverse( sub, op, keys, data );
            }
        }
        else {
            // TODO make the throwing of this Exception be something handled / optional by the travesr
            //  Aka combine the typeOk logic with this
            //  If type != ok options are :
            //    Do nothing
            //    Throw Exception
            //    "make" the type ok by overwriting with a new container object (got list, wanted map, so trash the list by overwriting with a new map)
            throw new TraversrException( "Type mismatch on parent." );
        }
    }
}
