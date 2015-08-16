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
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.List;

public abstract class RemovrSpec {

    protected final MatchablePathElement pathElement;

    public RemovrSpec(String rawJsonKey) {
        PathElement pathElement = parse( rawJsonKey );

        if (!(pathElement instanceof MatchablePathElement)) {
            throw new SpecException("Spec LHS key=" + rawJsonKey + " is not a valid LHS key.");
        }

        this.pathElement = (MatchablePathElement) pathElement;
    }

    // Ex Keys :  *, cdv-*, *-$de
    public static PathElement parse(String key) {
        if ( "*".equals( key ) ) {
            return new StarAllPathElement( key );
        }

        int numOfStars = StringTools.countMatches( key, "*" );
        if (numOfStars == 1) {
            return new StarSinglePathElement(key);
        } else if (numOfStars == 2) {
            return new StarDoublePathElement(key);
        } else if (numOfStars > 2) {
            return new StarRegexPathElement(key);
        } else {
            return new LiteralPathElement(key);
        }
    }

    protected Integer getIntegerFromLiteralPathElement() {

        Integer pathElementInt = null;

        try {
            pathElementInt = Integer.parseInt( pathElement.getRawKey() );
        }
        catch( NumberFormatException nfe ) {
            // If the data is an Array, but the spec keys are Non-Integer Strings,
            //  we are annoyed, but we don't stop the whole transform.
            // Just this part of the Transform won't work.
        }
        return pathElementInt;
    }

    public abstract List<Integer> applySpec(Object input);
}
