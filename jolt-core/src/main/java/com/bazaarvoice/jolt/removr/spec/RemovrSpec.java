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
package com.bazaarvoice.jolt.removr.spec;

import com.bazaarvoice.jolt.common.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.pathelement.StarSinglePathElement;
import com.bazaarvoice.jolt.common.pathelement.StarAllPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarDoublePathElement;
import com.bazaarvoice.jolt.common.pathelement.StarRegexPathElement;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarPathElement;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class RemovrSpec {

    protected final MatchablePathElement pathElement;

    public RemovrSpec(String rawJsonKey) {
        List<PathElement> pathElements = parse(rawJsonKey);

        if (pathElements.size() != 1) {
            throw new SpecException("Removr invalid LHS:" + rawJsonKey + " can not contain '.'");
        }

        PathElement pe = pathElements.get(0);
        if (!(pe instanceof MatchablePathElement)) {
            throw new SpecException("Spec LHS key=" + rawJsonKey + " is not a valid LHS key.");
        }

        this.pathElement = (MatchablePathElement) pe;
    }

    // Ex Keys :  *, cdv-*, *-$de
    public static List<PathElement> parse(String key) {
        if ( "*".equals( key ) ) {
            return Arrays.<PathElement>asList(new StarAllPathElement(key));
        }

        int numOfStars = StringTools.countMatches(key, "*");
        if (numOfStars == 1) {
            return Arrays.<PathElement>asList(new StarSinglePathElement(key));
        } else if (numOfStars == 2) {
            return Arrays.<PathElement>asList(new StarDoublePathElement(key));
        } else if (numOfStars > 2) {
            return Arrays.<PathElement>asList(new StarRegexPathElement(key));
        } else {
            return Arrays.<PathElement>asList(new LiteralPathElement(key));
        }
    }

    /** +
     * @input : Input map from which the literal/computed keys that match the Spec needs to be removed.
     * For starpathelements, go through all the input keys and check whether this pathelement key is a match.
     */

    public List<String> findKeysToBeRemoved(Map<String, Object> input) {
        ArrayList<String> keysToBeRemoved = new ArrayList<String>();
        boolean isStarPathElement = pathElement instanceof StarPathElement;
        for (String ipkey : input.keySet()) {
            if (isStarPathElement) {
                if ( ( (StarPathElement) pathElement).stringMatch( ipkey ) ) {
                    keysToBeRemoved.add(ipkey);
                }
            } else {

                keysToBeRemoved.add(pathElement.getRawKey());
            }
        }
        return keysToBeRemoved;
    }

    public abstract void remove(Map<String, Object> input);

    public abstract void removeByKey(Map<String, Object> inputMap, String key);

}
