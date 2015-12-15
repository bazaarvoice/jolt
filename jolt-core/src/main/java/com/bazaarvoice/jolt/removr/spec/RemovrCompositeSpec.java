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
import com.bazaarvoice.jolt.exception.SpecException;

import java.util.*;

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
        ArrayList<RemovrSpec> all = new ArrayList<>();

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

    /** +
     *
     * @param input : Pass in the input map from which the spec raw key has to remove itself if it matches.
     */
    @Override
    public void remove(Map<String, Object> input){
        if(pathElement instanceof LiteralPathElement){
            handleLiterals(input);
        }else{
            handleComputed(input);
        }
    }

    @Override
    public void removeJsonArrayFields(List<LinkedHashMap<String, Object>> input) {
        if (pathElement instanceof  LiteralPathElement) {
            for (LinkedHashMap<String, Object> list : input) {
                handleLiterals(list);
            }
        } else {
            for (LinkedHashMap<String, Object> list : input) {
                handleComputed(createMapForList(list));
            }
        }
    }

    /** +
     *
     * @param inputMap : Input map from which the spec keys need to be removed
     * @param literalKey : Literal Key to be removed from the map
     */
    @Override
    public void removeByKey(Map<String, Object> inputMap, String literalKey){
        Object subInput = inputMap.get(literalKey);
        if(subInput instanceof Map){
            for(RemovrSpec childSpec : allChildren){
                // Recursive call if composite spec, else removes the element from the map if it is a leaf spec.
                childSpec.remove((Map<String, Object>) subInput);
            }
        } else if (subInput instanceof List) {
            for(RemovrSpec childSpec : allChildren) {
                childSpec.removeJsonArrayFields((List<LinkedHashMap<String, Object>>) subInput);
            }
        }
    }

    private void handleLiterals(Map<String, Object> input){
        if(input == null)
            return;
        removeByKey(input, pathElement.getRawKey());
    }

    private void handleComputed(Map<String, Object> input){
        List<String> keysToBeRemoved = findKeysToBeRemoved(input);
        for(String key:keysToBeRemoved){
            if(input.get(key) instanceof Map){
                removeByKey(input,key);
            }
        }
    }
}
