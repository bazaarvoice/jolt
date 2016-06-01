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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Non-greedy * based Path Element.
 */
public class StarRegexPathElement extends BasePathElement implements StarPathElement {

    private final Pattern pattern;

    public StarRegexPathElement( String key ) {
        super(key);

        pattern = makePattern( key );
    }


    private static Pattern makePattern( String key ) {

        // "rating-*-*"  ->  "^rating-(.+?)-(.+?)$"   aka the '*' must match something in a non-greedy way
        key = escapeMetacharsIfAny(key);
        String regex = "^" + key.replace("*", "(.+?)")  + "$";

        /*
            wtf does "(.+?)" mean
            .  : match any character
            +  : match one or more of the previous thing
            ?  : match zero of one of the previous thing
            +? : reluctantly match

            See http://docs.oracle.com/javase/tutorial/essential/regex/quant.html
              Differences Among Greedy, Reluctant, and Possessive Quantifiers section
        */

        return Pattern.compile( regex);
    }

    // Metachars to escape .^$|*+?()[{\ in a regex

    /** +
     *
     * @param key : String key that needs to be escaped before compiling into regex.
     * @return : Metachar escaped key.
     *
     * Regex has some special meaning for the metachars [ .^$|*+?()[{\ ].If any of these metachars is present in the pattern key that was passed, it needs to be escaped so that
     * it can be matched against literal.
     */
    private static String escapeMetacharsIfAny(String key){

        char[] keyChars = key.toCharArray();

        // String.replace replaces all instances of the char sequence. So, it would try to escape the occurrence as many times as the occurrence frequency.
        // For ex: if a key as 2 '5star.rating.1', it would escape it twice resulting in 5star//.rating//.1.
        // So, we keep an list of already seen characters.

        Set<Character> charsAlreadySeen = new HashSet<>();

        for(char keychar: keyChars) {

            switch (keychar) {

                case '(':
                case '[':
                case '{':
                case '\\':
                case '^':
                case '$':
                case '|':
                case ')':
                case '?':
                case '+':
                case '.':

                    if(!charsAlreadySeen.contains( keychar )){

                        key = key.replace(String.valueOf(keychar), "\\" + keychar);

                        charsAlreadySeen.add(keychar);
                    }
                    break;

                default:
                    break;
            }
        }
        return key;
    }

    /**
     * @param literal test to see if the provided string will match this Element's regex
     * @return true if the provided literal will match this Element's regex
     */
    @Override
    public boolean stringMatch( String literal ) {

        Matcher matcher = pattern.matcher( literal );

        return matcher.find();
    }

    @Override
    public MatchedElement match( String dataKey, WalkedPath walkedPath ) {

        Matcher matcher = pattern.matcher( dataKey );
        if ( ! matcher.find() ) {
            return null;
        }

        int groupCount = matcher.groupCount();

        List<String> subKeys = new ArrayList<>(groupCount);
        for ( int index = 1; index <= groupCount; index++) {
            subKeys.add( matcher.group( index ) );
        }

        return new MatchedElement(dataKey, subKeys);
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }
}
