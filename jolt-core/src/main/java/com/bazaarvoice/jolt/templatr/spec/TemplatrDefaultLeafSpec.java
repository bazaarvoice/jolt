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

package com.bazaarvoice.jolt.templatr.spec;

import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.OpMode;

import java.util.List;
import java.util.Map;

/**
 * This spec copies default/hardcoded values from spec onto input object
 */
public class TemplatrDefaultLeafSpec extends TemplatrSpec {

    private final Object defaultValue;

    public TemplatrDefaultLeafSpec( final String rawJsonKey, Object rhs, final OpMode opMode  ) {
        super(rawJsonKey, opMode);
        //  leaf level is an actual string, non-string, null, List or Map value, we need to set as default
        if(rhs instanceof String || rhs instanceof Number || rhs instanceof Map || rhs instanceof List || rhs == null) {
            defaultValue = rhs;
        }
        else {
            throw new SpecException( "Invalid Templatr spec RHS. Should be literal value or string path leading with ^ to look up value from context or List or Map value to set as default. Spec in question " + rawJsonKey + " : " + rhs );
        }
    }

    @Override
    public boolean applyElement( final String inputKey, final Object input, final MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context ) {
        Object parent = walkedPath.lastElement().getTreeRef();
        setData( parent, thisLevel, defaultValue, opMode);
        return true;
    }
}
