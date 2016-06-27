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

package com.bazaarvoice.jolt.templatr;

import com.bazaarvoice.jolt.common.spec.SpecBuilder;
import com.bazaarvoice.jolt.templatr.spec.TemplatrCompositeSpec;
import com.bazaarvoice.jolt.templatr.spec.TemplatrContextLookupLeafSpec;
import com.bazaarvoice.jolt.templatr.spec.TemplatrDefaultLeafSpec;
import com.bazaarvoice.jolt.templatr.spec.TemplatrSelfLookupLeafSpec;
import com.bazaarvoice.jolt.templatr.spec.TemplatrSpec;

import java.util.Map;

public class TemplatrSpecBuilder extends SpecBuilder<TemplatrSpec> {

    private static final String CARET = "^";
    private static final String AT = "@";

    private final OpMode opMode;

    public TemplatrSpecBuilder(OpMode opMode) {
        this.opMode = opMode;
    }

    @Override
    public TemplatrSpec createSpec( final String lhs, final Object rhs ) {
        if( rhs instanceof Map ) {
            Map rhsMap = (Map)rhs;
            if(rhsMap.isEmpty()) {
                return new TemplatrDefaultLeafSpec( lhs, rhsMap, opMode );
            }
            else {
                return new TemplatrCompositeSpec(lhs, rhsMap, opMode, this );
            }

        }
        else {
            if ( rhs instanceof String ) {
                String rhsValue = (String) rhs;
                // leaf level starts with ^ , so spec is an dot notation read from context
                if(rhsValue.startsWith( CARET )) {
                    return new TemplatrContextLookupLeafSpec( lhs, rhsValue, opMode );
                }
                // leaf level starts with @ , so spec is an dot notation read from self
                else if(rhsValue.startsWith( AT )) {
                    return new TemplatrSelfLookupLeafSpec( lhs, rhsValue, opMode );
                }
                // leaf level is an actual string value, we need to set as default
                else {
                    return new TemplatrDefaultLeafSpec( lhs, rhsValue, opMode );
                }
            }
            // leaf level is an actual non-string value or null or Map or List, we need to set as default
            else {
                return new TemplatrDefaultLeafSpec( lhs, rhs, opMode );
            }
        }
    }
}
