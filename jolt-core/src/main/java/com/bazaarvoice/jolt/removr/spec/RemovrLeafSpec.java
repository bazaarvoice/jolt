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

import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.common.pathelement.*;
import com.bazaarvoice.jolt.shiftr.ShiftrWriter;
import org.apache.commons.lang.StringUtils;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.WalkedPath;

import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** +
 * Spec for handling the leaf spec in Removr Transforms.
 */
public class RemovrLeafSpec extends RemovrSpec {


    public RemovrLeafSpec( String rawKey ) {
        super( rawKey );
    }


    /** +
     *
     * @param input : Input map from which the spec key needs to be removed.
     */
    @Override
    public void remove(Map<String, Object> input ){

       if(!(input instanceof Map))
           throw new SpecException("Expected a map. Got input of type: " + input.getClass().getName());

           List<String> keysToBeRemoved = getKeysToBeRemoved(input);
            for (String key :keysToBeRemoved){
                removeByKey(input, key);
            }
    }

    /** +
     *
     * @param inputMap: Input map
     * @param key: Key that needs to be removed from the key.
     */
    @Override
    public void removeByKey(Map<String, Object> inputMap, String key) {
        inputMap.remove(key);
    }
}
