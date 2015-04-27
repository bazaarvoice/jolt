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
package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.common.WalkedPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LiteralPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final List<String> subKeys;

    private int hashCount = 0;

    public LiteralPathElement( String key ) {
        super(key);

        List<String> keys = new ArrayList<>(1);
        keys.add( key ); // always add the full key to index 0

        this.subKeys = Collections.unmodifiableList( keys );
    }

    public LiteralPathElement( String key, List<String> subKeys ) {
        super(key);

        if ( subKeys == null ) {
            throw new IllegalArgumentException( "LiteralPathElement for key:" + key + " got null list of subKeys" );
        }

        List<String> keys = new ArrayList<>( 1 + subKeys.size() );
        keys.add( key ); // always add the full key to index 0
        keys.addAll( subKeys );

        this.subKeys = Collections.unmodifiableList( keys );
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {
        return getRawKey();
    }

    @Override
    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        if ( getRawKey().equals( dataKey ) ) {
            return new LiteralPathElement( getRawKey(), subKeys );
        }
        return null;
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }

    public String getSubKeyRef( int index ) {
        if ((index < 0) || (index >= this.subKeys.size())) {
            throw new IndexOutOfBoundsException( "LiteralPathElement "+ this.subKeys +" cannot be indexed with index "+index );
        }
        return subKeys.get( index );
    }

    public int getSubKeyCount(){
        return subKeys.size();
    }

    public int getHashCount() {
        return hashCount;
    }

    public void incrementHashCount() {
        hashCount++;
    }
}
