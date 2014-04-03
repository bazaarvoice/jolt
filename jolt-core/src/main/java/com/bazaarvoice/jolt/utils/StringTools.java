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

package com.bazaarvoice.jolt.utils;

/**
 *
 * This class mimics the behavior of apache StringTools, except that it works on CharSequence instead of String
 *
 * Also, with this, jolt-core can finally be free of apache-common dependency
 */
public class StringTools {

    /**
     * Count the num# of matches of subSequence in sourceSequence
     *
     * @param sourceSequence to find occurrence from
     * @param subSequence to find occurrence of
     * @return num of occurrences of subSequence in sourceSequence
     */
    public static int countMatches(CharSequence sourceSequence, CharSequence subSequence) {
        if (isEmpty(sourceSequence) || isEmpty(subSequence) || sourceSequence.length() < subSequence.length()) {
            return 0;
        }

        int count = 0;
        int sourceSequenceIndex = 0;
        int subSequenceIndex = 0;

        while(sourceSequenceIndex < sourceSequence.length()) {
            if(sourceSequence.charAt(sourceSequenceIndex) == subSequence.charAt(subSequenceIndex)) {
                sourceSequenceIndex++;
                subSequenceIndex++;
                while(sourceSequenceIndex < sourceSequence.length() && subSequenceIndex < subSequence.length()) {
                    if(sourceSequence.charAt(sourceSequenceIndex) != subSequence.charAt(subSequenceIndex)) {
                        break;
                    }
                    sourceSequenceIndex++;
                    subSequenceIndex++;
                }
                if(subSequenceIndex == subSequence.length()) {
                    count++;
                }
                subSequenceIndex = 0;
                continue;
            }
            sourceSequenceIndex++;
        }

        return count;
    }

    /**
     * Check if a sequence is NOT blank
     *
     * @param sourceSequence to check
     * @return true if sourceSequence is NOT blank
     */
    public static boolean isNotBlank(CharSequence sourceSequence) {
        return !isBlank(sourceSequence);
    }

    /**
     * Check if a sequence is blank
     *
     * @param sourceSequence to check
     * @return true is sourceSequence is blank
     */
    public static boolean isBlank(CharSequence sourceSequence) {
        int sequenceLength;
        if (sourceSequence == null || (sequenceLength = sourceSequence.length()) == 0) {
            return true;
        }
        for (int i = 0; i < sequenceLength; i++) {
            if ((Character.isWhitespace(sourceSequence.charAt(i)) == false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if a sequence is empty
     *
     * @param sourceSequence to check
     * @return true if source is empty
     */
    public static boolean isEmpty(CharSequence sourceSequence) {
        return sourceSequence == null || sourceSequence.length() == 0;
    }
}
