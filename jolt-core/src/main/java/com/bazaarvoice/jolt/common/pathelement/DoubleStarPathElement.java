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
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sumithra.gomadam
 * Date: 11/14/13
 * Time: 2:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class DoubleStarPathElement extends BasePathElement implements StarPathElement {

    private final String prefix,suffix, mid;

    public DoubleStarPathElement( String key ) {
        super(key);

        if ( StringUtils.countMatches(key, "*") != 2 ) {
            throw new IllegalArgumentException( "DoubleStarPathElement should have two '*' in its key." );
        }
        else if ( "*".equals( key ) ) {
            throw new IllegalArgumentException( "DoubleStarPathElement should have a key that has two '*'." );
        }

        //Using the StringUtils instead of the string split function to remove the empty entries.
        String[] split = StringUtils.split(key, '*');
        if ( key.startsWith( "*" )  && key.endsWith("*")) {
            prefix = "";
            mid = split[0];
            suffix = "";
        }
        else if ( key.endsWith( "*" ) ) {
            prefix = split[0];
            mid = split[1];
            suffix = "";
        }
        else if ( key.startsWith( "*" ) ) {
            prefix = "";
            mid = split[0];
            suffix = split[1];
        }
        else{
            prefix=split[0];
            mid=split[1];
            suffix=split[2];
        }
    }
    @Override
    public boolean stringMatch(String literal) {
        boolean isMatch = false;
        if(literal.startsWith(prefix) && literal.endsWith(suffix) && literal.contains(mid)){
            int prefixEnd = prefix.length() - 1;
            int midStart = literal.indexOf(mid);
            int midEnd = literal.indexOf(mid) + mid.length();
            int suffixStart = literal.length() - suffix.length();

            if((midStart - prefixEnd > 0) && (suffixStart - midEnd > 0)){
                isMatch = true;
            }

        }
        return isMatch;
    }

    @Override
    public LiteralPathElement match(String dataKey, WalkedPath walkedPath) {
        if ( stringMatch( dataKey ) )  {
            List<String> subKeys = new ArrayList<String>(2);

            int midStart = dataKey.indexOf(mid);
            int midEnd = dataKey.indexOf(mid) + mid.length();

            String firstStarPart = dataKey.substring( prefix.length(), midStart);
            subKeys.add( firstStarPart );

            String secondStarPart = dataKey.substring( midEnd, dataKey.length() - suffix.length()  );
            subKeys.add( secondStarPart );

            return new LiteralPathElement(dataKey, subKeys);
        }

        return null;
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }
}
