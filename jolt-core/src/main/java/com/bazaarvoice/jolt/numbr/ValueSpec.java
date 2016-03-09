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

import java.util.HashMap;
import java.util.Map;

/**
 * Leaf-level spec for converting numeric values.
 */
abstract public class ValueSpec implements Spec {

    private final static Map<String, ValueSpec> _instanceTypeMap = initializeInstanceTypeMap();

    private static Map<String, ValueSpec> initializeInstanceTypeMap() {
        Map<String, ValueSpec> instanceTypeMap = new HashMap<>();
        instanceTypeMap.put("int", new IntValueSpec());
        instanceTypeMap.put("long", new LongValueSpec());
        instanceTypeMap.put("float", new FloatValueSpec());
        instanceTypeMap.put("double", new DoubleValueSpec());
        return instanceTypeMap;
    }

    private static class IntValueSpec extends ValueSpec {
        @Override
        protected Number transformNumber(Number number) {
            return number.intValue();
        }
    }

    private static class LongValueSpec extends ValueSpec {
        @Override
        protected Number transformNumber(Number number) {
            return number.longValue();
        }
    }

    private static class FloatValueSpec extends ValueSpec {
        @Override
        protected Number transformNumber(Number number) {
            return number.floatValue();
        }
    }

    private static class DoubleValueSpec extends ValueSpec {
        @Override
        protected Number transformNumber(Number number) {
            return number.doubleValue();
        }
    }

    public static ValueSpec forType(String type) {
        ValueSpec valueSpec = _instanceTypeMap.get(type);
        if (valueSpec == null) {
            throw new IllegalArgumentException("Unsupported numeric type: " + type);
        }
        return valueSpec;
    }

    abstract protected Number transformNumber(Number number);

    @Override
    public Object transform(Object o) {
        if (o instanceof Number) {
            return transformNumber((Number) o);
        }
        return o;
    }
}