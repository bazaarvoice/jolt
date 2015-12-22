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

import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarAllPathElement;
import com.bazaarvoice.jolt.common.pathelement.StarPathElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** +
 * Spec for handling the leaf spec of the Removr Transform.
 */
public class RemovrLeafSpec extends RemovrSpec {

    public RemovrLeafSpec( String rawKey ) {
        super( rawKey );
    }

    /**
     * @param inputMap : Input map from which the spec key needs to be removed.
     */
    @Override
    public List<String> applySpec( Map<String,Object> inputMap ) {
        if ( inputMap == null ) {
            return null;
        }


        if ( pathElement instanceof LiteralPathElement ) {
            inputMap.remove( pathElement.getRawKey() );
        }
        else if ( pathElement instanceof StarPathElement ) {

            List<String> keysToBeRemoved = new LinkedList<>();
            for( Map.Entry<String,Object> entry : inputMap.entrySet() ) {

                StarPathElement star = (StarPathElement) pathElement;

                if ( star.stringMatch( entry.getKey() ) ) {
                    keysToBeRemoved.add( entry.getKey() );
                }
            }

            for ( String key : keysToBeRemoved ) {
                inputMap.remove( key );
            }
        }

        return Collections.emptyList();
    }



    /**
     * @param inputList : Input map from which the spec key needs to be removed.
     */
    @Override
    public List<Integer> applySpec( List<Object> inputList ) {
        if ( inputList == null ) {
            return null;
        }

        if ( pathElement instanceof LiteralPathElement ) {

            Integer pathElementInt = getNonNegativeIntegerFromLiteralPathElement();

            if ( pathElementInt != null && pathElementInt < inputList.size() ) {
                return Collections.singletonList( pathElementInt );
            }
        }
        else if ( pathElement instanceof StarAllPathElement ) {

            // To be clear, this is kinda silly.
            // If you just wanted to remove the whole list, you could have just
            //  directly removed it, instead of stepping into it and using the "*".
            List<Integer> toReturn = new ArrayList<>( inputList.size() );
            for( int index = 0; index < inputList.size(); index++ ) {
                toReturn.add( index );
            }

            return toReturn;
        }

         return Collections.emptyList();
    }
}
