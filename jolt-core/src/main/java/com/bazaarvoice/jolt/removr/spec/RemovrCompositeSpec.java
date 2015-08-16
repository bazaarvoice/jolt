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
import com.bazaarvoice.jolt.exception.SpecException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
    Sample Spec
    "spec": {
        "ineedtoberemoved":"" //literal leaf element
        "TAG-*$*": "",       //Leaf Computed element
        "TAG-*#*": "",

        "*pants*" : "",

         "buckets": {     //composite literal Path element
            "a$*": ""    //Computed Leaf element
         },
         "rating*":{    //composite computed path element
            "*":{       //composite computed path element
                "a":""  //literal leaf element
            }
        }
    }
*/

/**
 *  Removr Spec that has children. In a removr spec, whenever the RHS is a Map, we build a RemovrCompositeSpec
 */
public class RemovrCompositeSpec extends RemovrSpec {

    private final List<RemovrSpec> allChildren;

    public RemovrCompositeSpec(String rawKey, Map<String, Object> spec ) {
        super( rawKey );
        List<RemovrSpec> all = new ArrayList<>();

        for ( String rawLhsStr : spec.keySet() ) {
            Object rawRhs = spec.get( rawLhsStr );
            String[] keyStrings = rawLhsStr.split( "\\|" );
            for ( String keyString : keyStrings ) {
                RemovrSpec childSpec;
                if( rawRhs instanceof Map ) {
                    childSpec = new RemovrCompositeSpec(keyString, (Map<String, Object>) rawRhs );
                }
                else if (rawRhs instanceof String && ((String)rawRhs).trim().length() == 0) {
                    childSpec = new RemovrLeafSpec(keyString);
                }
                else{
                    throw new SpecException("Invalid Removr spec RHS. Should be an empty string or Map");
                }
                all.add(childSpec);
            }
        }
        allChildren = Collections.unmodifiableList( all );
    }

    /**
     * @param input : Pass in the input map from which the spec raw key has to remove itself if it matches.
     */
    @Override
    public List<Integer> applySpec( Object input ) {

        if ( input instanceof Map ) {

            Map<String, Object> inputMap = (Map<String, Object>) input;

            if ( pathElement instanceof LiteralPathElement ) {
                Object subInput = inputMap.get( pathElement.getRawKey() );
                callChildren( allChildren, subInput );
            }
            else if ( pathElement instanceof StarPathElement ) {

                for( Map.Entry<String,Object> entry : inputMap.entrySet() ) {

                    StarPathElement star = (StarPathElement) pathElement;

                    if ( star.stringMatch( entry.getKey() ) ) {
                        callChildren( allChildren, entry.getValue() );
                    }
                }
            }
        }
        else if ( input instanceof List ) {
            List<Object> inputList = (List<Object>) input;

            // IF the input is a List, the only thing that will match is a Literal or a "*"
            if ( pathElement instanceof LiteralPathElement ) {

                Integer pathElementInt = getIntegerFromLiteralPathElement();

                if ( pathElementInt != null && pathElementInt < inputList.size() ) {
                    Object subObj = inputList.get( pathElementInt );
                    callChildren( allChildren, subObj );
                }
            }
            else if ( pathElement instanceof StarAllPathElement ) {
                for( Object entry : inputList ) {
                    callChildren( allChildren, entry );
                }
            }
        }

        // Composite Nodes always return an empty list, as they dont actually remove anything.
        return Collections.emptyList();
    }

    public void callChildren( List<RemovrSpec> children, Object subInput ) {

        if (subInput != null ) {

            if( subInput instanceof List ) {

                Set<Integer> indiciesToRemove = new HashSet<>();

                for(RemovrSpec childSpec : children) {
                    indiciesToRemove.addAll( childSpec.applySpec( subInput ) );
                }

                List<Integer> uniqueIndiciesToRemove = new ArrayList<>( indiciesToRemove );
                Collections.sort( uniqueIndiciesToRemove, new Comparator<Integer>() {
                    @Override
                    public int compare( Integer o1, Integer o2 ) {
                        return o2.compareTo( o1 );
                    }
                } );

                List<Object> subList = (List<Object>) subInput;
                for ( int index : uniqueIndiciesToRemove ) {
                    subList.remove( index );
                }
            }
            else {
                for(RemovrSpec childSpec : children) {
                    childSpec.applySpec( subInput );
                }
            }
        }
    }
}
