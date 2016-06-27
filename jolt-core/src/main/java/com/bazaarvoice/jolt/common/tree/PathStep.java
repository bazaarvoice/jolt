/*
 * Copyright 2014 Bazaarvoice, Inc.
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

import com.bazaarvoice.jolt.common.Optional;

/**
 * A tuple class that contains the data for one level of a
 *  tree walk, aka a reference to the input for that level, and
 *  the LiteralPathElement that was matched at that level.
 */
public final class PathStep {

    private final Object treeRef;
    private final MatchedElement matchedElement;
    private final Optional<Integer> origSize;

    public PathStep(Object treeRef, MatchedElement matchedElement ) {
        this.treeRef = treeRef;
        this.matchedElement = matchedElement;
        if (matchedElement instanceof ArrayMatchedElement) {
            origSize = Optional.of( ( (ArrayMatchedElement) matchedElement ).getOrigSize() );
        }
        else {
            origSize = Optional.empty();
        }
    }

    public Object getTreeRef() {
        return treeRef;
    }

    public MatchedElement getMatchedElement() {
        return matchedElement;
    }

    public Optional<Integer> getOrigSize() {
        return origSize;
    }
}
