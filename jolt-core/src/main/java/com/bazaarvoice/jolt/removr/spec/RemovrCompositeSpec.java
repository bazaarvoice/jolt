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
import java.util.LinkedList;
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

    private final List<RemovrSpec> allChildNodes;

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
        allChildNodes = Collections.unmodifiableList( all );
    }

    @Override
    public List<String> applyToMap( Map<String, Object> inputMap ) {

        if ( pathElement instanceof LiteralPathElement ) {
            Object subInput = inputMap.get( pathElement.getRawKey() );
            processChildren( allChildNodes, subInput );
        }
        else if ( pathElement instanceof StarPathElement ) {

            StarPathElement star = (StarPathElement) pathElement;

            // Compare my pathElement with each key from the input.
            // If it matches, recursively call process the child nodes.
            for( Map.Entry<String,Object> entry : inputMap.entrySet() ) {

                if ( star.stringMatch( entry.getKey() ) ) {
                    processChildren( allChildNodes, entry.getValue() );
                }
            }
        }

        // Composite Nodes always return an empty list, as they dont actually remove anything.
        return Collections.emptyList();
    }

    @Override
    public List<Integer> applyToList( List<Object> inputList ) {

        // IF the input is a List, the only thing that will match is a Literal or a "*"
        if ( pathElement instanceof LiteralPathElement ) {

            Integer pathElementInt = getNonNegativeIntegerFromLiteralPathElement();

            if ( pathElementInt != null && pathElementInt < inputList.size() ) {
                Object subObj = inputList.get( pathElementInt );
                processChildren( allChildNodes, subObj );
            }
        }
        else if ( pathElement instanceof StarAllPathElement ) {
            for( Object entry : inputList ) {
                processChildren( allChildNodes, entry );
            }
        }

        // Composite Nodes always return an empty list, as they dont actually remove anything.
        return Collections.emptyList();
    }

    /**
     * Call our child nodes, build up the set of keys or indices to actually remove, and then
     *  remove them.
     */
    private void processChildren( List<RemovrSpec> children, Object subInput ) {

        if (subInput != null ) {

            if( subInput instanceof List ) {

                List<Object> subList = (List<Object>) subInput;
                Set<Integer> indiciesToRemove = new HashSet<>();

                // build a list of all indicies to remove
                for(RemovrSpec childSpec : children) {
                    indiciesToRemove.addAll( childSpec.applyToList( subList ) );
                }

                List<Integer> uniqueIndiciesToRemove = new ArrayList<>( indiciesToRemove );
                // Sort the list from Biggest to Smallest, so that when we remove items from the input
                //  list we don't muck up the order.
                // Aka removing 0 _then_ 3 would be bad, because we would have actually removed
                //  0 and 4 from the "original" list.
                Collections.sort( uniqueIndiciesToRemove, new Comparator<Integer>() {
                    @Override
                    public int compare( Integer o1, Integer o2 ) {
                        return o2.compareTo( o1 );
                    }
                } );

                for ( int index : uniqueIndiciesToRemove ) {
                    subList.remove( index );
                }
            }
            else if (subInput instanceof Map ) {

                Map<String,Object> subInputMap = (Map<String,Object>) subInput;

                List<String> keysToRemove = new LinkedList<>();

                for(RemovrSpec childSpec : children) {
                    keysToRemove.addAll( childSpec.applyToMap( subInputMap ) );
                }

                subInputMap.keySet().removeAll( keysToRemove );
            }
        }
    }
}
