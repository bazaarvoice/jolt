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

package com.bazaarvoice.jolt.common;

import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

/**
 * Builds Traversal based on specific implementation of build(String path)
 * i.e. In Shiftr, it creates a ShiftrWriter, which internally uses ShiftrTraversr
 * to handle shiftr style inserts, and in Templatr, it create a basic TransposeReader
 * which uses SimpleTraversr for regular read/writes
 */
public abstract class TraversalBuilder {

    public <T extends PathEvaluatingTraversal> T build( Object rawObj ) {

        if ( ! ( rawObj instanceof String ) ) {
            throw new SpecException( "Invalid spec, RHS should be a String or array of Strings. Value in question : " + rawObj );
        }

        // Prepend "root" to each output path.
        // This is needed for the "identity" transform, eg if we are just supposed to put the input into the output
        //  what key do we put it under?
        String outputPathStr = (String) rawObj;
        if ( StringTools.isBlank( outputPathStr ) ) {
            outputPathStr = SpecDriven.ROOT_KEY;
        }
        else {
            outputPathStr = SpecDriven.ROOT_KEY + "." + outputPathStr;
        }

        return buildFromPath( outputPathStr );
    }

    /**
     * Given a path to traverse, and based on what Type T of traverser requested,
     * build and appropriate traversr
     * @param path to trvarse
     * @param <T> Type of Traversr required
     * @return a Traversr of type T that con traverse given path
     */
    public abstract <T extends PathEvaluatingTraversal> T buildFromPath( String path );
}
