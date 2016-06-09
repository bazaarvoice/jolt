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
package com.bazaarvoice.jolt.numbr;

import java.util.ArrayList;
import java.util.List;

/**
 * Numbr spec implementation for an array.  If the matching value is an array it will apply the provided
 * spec's transform to all elements of the array.
 */
public class ArraySpec implements Spec {

    public final static String SPEC_SUFFIX = "[]";

    private final Spec _childSpec;

    public ArraySpec(Spec childSpec) {
        _childSpec = childSpec;
    }

    @Override
    public Object transform(Object o) {
        if (o instanceof List) {
            //noinspection unchecked
            List<Object> list = (List<Object>) o;
            List<Object> transformed = new ArrayList<>(list.size());

            for (Object element : list) {
                Object elementTransformed = _childSpec.transform(element);
                transformed.add(elementTransformed);
            }

            return transformed;
        }

        return o;
    }
}
