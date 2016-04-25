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
 *  PathElement for the a double "*" wildcard such as tag-*-*.   In this case we can avoid doing any
 *  regex work by doing String begins, ends and mid element exists.
 */
public class StarDoublePathElement extends BasePathElement implements StarPathElement {

    private final String prefix,suffix, mid;

    /**+
     *
     * @param key : should be a String with two "*" elements.
     */
    public StarDoublePathElement(String key) {
        super(key);

        if ( StringTools.countMatches(key, "*") != 2 ) {
            throw new IllegalArgumentException( "StarDoublePathElement should have two '*' in its key. Was: " + key );
        }

        String[] split = key.split("\\*");
        boolean startsWithStar = key.startsWith( "*" );
        boolean endsWithStar = key.endsWith("*");
        if (  startsWithStar && endsWithStar) {
            prefix = "";
            mid = split[1];
            suffix = "";
        }
        else if ( endsWithStar ) {
            prefix = split[0];
            mid = split[1];
            suffix = "";
        }
        else if ( startsWithStar ) {
            prefix = "";
            mid = split[1];
            suffix = split[2];
        }
        else{
            prefix=split[0];
            mid=split[1];
            suffix=split[2];
        }
    }
    /**
     * @param literal test to see if the provided string will match this Element's regex
     * @return true if the provided literal will match this Element's regex
     */
    @Override
    public boolean stringMatch(String literal) {
        boolean isMatch = false;
        if(literal.startsWith(prefix) && literal.endsWith(suffix)){

            isMatch = finMidIndex(literal) > 0;
        }
        return isMatch;
    }

    /**
     * The assumption here is: * means 1 or more characters. So, if we can find the mid 1 char after the prefix ends and 1 char before the suffix
     * starts, we have found a mid match. Also, it will be the first occurrence of the mid in the literal, so we are not 'greedy' to capture as much as
     * in the '*'
     */
    private int finMidIndex(String literal){
        int startOffset = prefix.length() + 1;
        int endOffset = literal.length() - suffix.length() - 1;

        /**
         * Found a bug when there is only character after the prefix ends. For eg: if the spec is abc-*$* and the key
         * we got is abc-1
         *      prefix -> abc-
         *      suffix -> ""
         *      mid    -> $
         *      startoffset -> 5
         *      endoffset -> 5 - 0 - 1 = 4
         *  We are left with no substring to search for the mid. Bail out!
         */
        if(startOffset >= endOffset)  {

            return -1;

        }
        int midIndex = literal.substring(startOffset, endOffset).indexOf(mid);

        if(midIndex >= 0) {

            return midIndex +  startOffset;
        }
        return -1;
    }


    @Override
    public MatchedElement match(String dataKey, WalkedPath walkedPath) {
        if ( stringMatch( dataKey ) )  {
            List<String> subKeys = new ArrayList<>(2);

            int midStart = finMidIndex(dataKey);
            int midEnd = midStart + mid.length();

            String firstStarPart = dataKey.substring( prefix.length(), midStart);
            subKeys.add( firstStarPart );

            String secondStarPart = dataKey.substring( midEnd, dataKey.length() - suffix.length()  );
            subKeys.add( secondStarPart );

            return new MatchedElement(dataKey, subKeys);
        }
        return null;
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }
}
