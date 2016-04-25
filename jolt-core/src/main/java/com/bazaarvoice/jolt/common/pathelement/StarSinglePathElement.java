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

import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.ArrayList;
import java.util.List;

/**
 * PathElement for the a single "*" wildcard such as tag-*.   In this case we can avoid doing any
 *  regex work by doing String begins and ends with comparisons.
 */
public class StarSinglePathElement extends BasePathElement implements StarPathElement {

    private final String prefix,suffix;

    public StarSinglePathElement( String key ) {
        super(key);

        if ( StringTools.countMatches(key, "*") != 1 ) {
            throw new IllegalArgumentException( "StarSinglePathElement should only have one '*' in its key. Was: " + key );
        }
        else if ( "*".equals( key ) ) {
            throw new IllegalArgumentException( "StarSinglePathElement should have a key that is just '*'. Was: " + key );
        }

        if ( key.startsWith( "*" ) ) {
            prefix = "";
            suffix = key.substring( 1 );
        }
        else if ( key.endsWith( "*" ) ) {
            prefix = key.substring( 0, key.length() -1 );
            suffix = "";
        }
        else
        {
            String[] split = key.split( "\\*" );
            prefix = split[0];
            suffix = split[1];
        }
    }

    /**
     * @param literal test to see if the provided string will match this Element's regex
     * @return true if the provided literal will match this Element's regex
     */
    @Override
    public boolean stringMatch( String literal ) {
        return literal.startsWith( prefix ) && literal.endsWith( suffix )  // the ends match
                && literal.length() > prefix.length() + suffix.length();   // and the * captures something
    }

    @Override
    public MatchedElement match( String dataKey, WalkedPath walkedPath ) {

        if ( stringMatch( dataKey ) )  {
            List<String> subKeys = new ArrayList<>(1);

            String starPart = dataKey.substring( prefix.length(), dataKey.length() - suffix.length() );
            subKeys.add( starPart );

            return new MatchedElement(dataKey, subKeys);
        }

        return null;
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }
}
