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

import java.util.Iterator;

/**
 * A step in a JSON tree traversal.
 */
public interface TraversalStep<StepType, DataType> {

    /**
     * The three things you can do with a Traversal.
     */
    public enum Operation { SET, GET, REMOVE }

    /**
     * Return the data for the key from the provided tree object.
     *
     * @return data object if available, or null.
     */
    public Optional<DataType> get( StepType tree, String key );

    /**
     * Remove and return the data for the key from the provided tree object.
     *
     * @return data object if available, or null.
     */
    public Optional<DataType> remove( StepType tree, String key );

    /**
     * Insert the data into the tree, overwriting any data that is there.
     *
     * @return returns the data object if successful or null if it could not
     */
    public Optional<DataType> overwriteSet( StepType tree, String key, DataType data );

    /**
     * @return the child Traversal or null if this Traversal has no child
     */
    public TraversalStep getChild();

    /**
     * Create a new mutable Map or List, suitable for this PathElement to traverse.
     *
     * @return new List or Map, depending on the type of the Traversal
     */
    public StepType newContainer();

    /**
     * Return the Class of the Generic T, so that it can be used in an
     *  "instanceof" style check.
     *
     * @return Class that matches Generic parameter T
     */
    public Class<?> getStepType();

    /**
     * The meat of the Traversal.
     *
     * Pull a key from the iterator, use it to make the traversal, and then
     *  call traverse on your child Traversal.
     *
     * @param tree tree of data to walk
     * @param op the Operation to perform is this is the last node of the Traversal
     * @param keys keys to use
     * @param data the data to place if the operation is SET
     * @return if SET, null for fail or the "data" object for ok.  if GET, PANTS
     */
    public Optional<DataType> traverse( StepType tree, Operation op, Iterator<String> keys, DataType data );
}
