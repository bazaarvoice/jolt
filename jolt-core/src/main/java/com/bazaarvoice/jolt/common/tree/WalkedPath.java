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
package com.bazaarvoice.jolt.common.tree;

import java.util.ArrayList;
import java.util.Collection;

/**
 * DataStructure used by a SpecTransform during it's parallel tree walk.
 *
 * Basically this is Stack that records the steps down the tree that have been taken.
 * For each level, there is a PathStep, which contains a pointer the data of that level,
 *  and a pointer to the LiteralPathElement matched at that level.
 *
 * At any given point in time, it represents where in the tree walk a Spec is operating.
 * It is primarily used to by the ShiftrLeafSpec and CardinalityLeafSpec as a reference
 * to lookup real values for output "&(1,1)" references.
 *
 * It is expected that as the SpecTransform navigates down the tree, MatchedElements will be added and then
 *  removed when that subtree has been walked.
 */
public class WalkedPath extends ArrayList<PathStep> {

    public WalkedPath() {
        super();
    }

    public WalkedPath(Collection<PathStep> c) {
        super(c);
    }

    public WalkedPath( Object treeRef, MatchedElement matchedElement ) {
        super();
        this.add( new PathStep( treeRef, matchedElement ) );
    }

    /**
     * Convenience method
     */
    public boolean add( Object treeRef, MatchedElement matchedElement ) {
        return super.add( new PathStep( treeRef, matchedElement ) );
    }

    public void removeLast() {
        remove(size() - 1);
    }

    /**
     * Method useful to "&", "&1", "&2", etc evaluation.
     */
    public PathStep elementFromEnd(int idxFromEnd) {
        if (isEmpty()) {
            return null;
        }
        return get(size() - 1 - idxFromEnd);
    }

    public PathStep lastElement() {
        return get(size() - 1);
    }
}
