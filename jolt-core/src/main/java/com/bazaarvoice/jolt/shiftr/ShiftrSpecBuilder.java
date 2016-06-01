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

package com.bazaarvoice.jolt.shiftr;

import com.bazaarvoice.jolt.common.spec.SpecBuilder;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrCompositeSpec;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrLeafSpec;
import com.bazaarvoice.jolt.shiftr.spec.ShiftrSpec;

import java.util.Map;

public class ShiftrSpecBuilder extends SpecBuilder<ShiftrSpec> {
    @SuppressWarnings( "unchecked" )
    @Override
    public ShiftrSpec createSpec( final String keyString, final Object rawRhs ) {
        if( rawRhs instanceof Map ) {
            return new ShiftrCompositeSpec(keyString, (Map<String, Object>) rawRhs );
        }
        else {
            return new ShiftrLeafSpec(keyString, rawRhs );
        }
    }
}
