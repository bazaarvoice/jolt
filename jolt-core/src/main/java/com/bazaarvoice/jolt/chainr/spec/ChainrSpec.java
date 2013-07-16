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
package com.bazaarvoice.jolt.chainr.spec;

import com.bazaarvoice.jolt.exception.SpecException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Helper class that encapsulates the Chainr spec's list.
 *
 * If I didn't want to keep Jackson from being a dependency, this would be the type of class that
 * I would have Jackson load for me.
 */
public class ChainrSpec {

    protected final List<ChainrEntry> chainrEntries;

    public ChainrSpec( Object chainrSpec ) {

        if ( !( chainrSpec instanceof List ) ) {
            throw new SpecException(  "JOLT Chainr expects a JSON array of objects - Malformed spec." );
        }

        @SuppressWarnings( "unchecked" ) // We know its a list due to the check above
        List<Object> operations = (List<Object>) chainrSpec;

        if ( operations.isEmpty() ) {
            throw new SpecException( "JOLT Chainr passed an empty JSON array.");
        }

        List<ChainrEntry> entries = new ArrayList<ChainrEntry>(operations.size());

        for ( int index = 0; index < operations.size(); index++ ) {

            Object chainrEntryObj = operations.get( index );

            ChainrEntry entry = new ChainrEntry( index, chainrEntryObj );

            entries.add( entry );
        }

        chainrEntries = Collections.unmodifiableList( entries );
    }

    public List<ChainrEntry> getChainrEntries() {
        return chainrEntries;
    }
}
