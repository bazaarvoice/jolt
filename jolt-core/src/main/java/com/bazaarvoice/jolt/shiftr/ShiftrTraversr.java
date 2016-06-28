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
package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.traversr.SimpleTraversr;
import com.bazaarvoice.jolt.traversr.traversal.TraversalStep;

import java.util.ArrayList;
import java.util.List;

/**
 * Traverser that does not overwrite data.
 */
public class ShiftrTraversr<DataType> extends SimpleTraversr<DataType> {

    public ShiftrTraversr( String humanPath ) {
        super( humanPath );
    }

    public ShiftrTraversr( List<String> paths ) {
        super( paths );
    }

    /**
     * Do a Shift style insert :
     *  1) if there is no data "there", then just set it
     *  2) if there is already a list "there", just add the data to the list
     *  3) if there something other than a list there, grab it and stuff it and the data into a list
     *     and overwrite what is there with a list.
     */
    public Optional<DataType> handleFinalSet( TraversalStep traversalStep, Object tree, String key, DataType data ) {

        Optional<DataType> optSub = traversalStep.get( tree, key );

        if ( !optSub.isPresent() || optSub.get() == null ) {
            // nothing is here so just set the data
            traversalStep.overwriteSet( tree, key, data );
        }
        else if ( optSub.get() instanceof List ) {
            // there is a list here, so we just add to it
            ((List<Object>) optSub.get()).add( data );
        }
        else {
            // take whatever is there and make it the first element in an Array
            List<Object> temp = new ArrayList<>();
            temp.add( optSub.get() );
            temp.add( data );

            traversalStep.overwriteSet( tree, key, temp );
        }

        return Optional.of( data );
    }
}
