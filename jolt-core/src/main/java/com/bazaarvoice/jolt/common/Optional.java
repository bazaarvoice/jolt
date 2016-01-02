/*
 * Copyright 2016 Bazaarvoice, Inc.
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
package com.bazaarvoice.jolt.common;

/**
 * This can go away when Jolt moves to Java 8.
 */
public class Optional {

    private final Object obj;
    private final boolean abs;

    public static Optional absent() {
        return new Optional();
    }

    public static Optional of( Object obj ) {
        return new Optional( obj );
    }

    private Optional() {
        obj = null;
        abs = true;
    }

    private Optional( Object obj ) {
        this.obj = obj;
        abs = false;
    }

    public Object get() {
        return obj;
    }

    public boolean isPresent() {
        return ! abs;
    }
}
