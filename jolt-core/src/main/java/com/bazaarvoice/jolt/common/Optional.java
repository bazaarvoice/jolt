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
 * We cannot go away from this Optional to java 8 Optional because, this Optional gives as three states!
 * Where a value can an Object, a null (which is a valid json value) and absent,
 * which is not possible with Java 8 Optional.
 */
public class Optional<T> {

    private final T obj;
    private final boolean abs;

    private static final Optional<?> EMPTY = new Optional<>();

    public static<T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    private Optional() {
        obj = null;
        abs = true;
    }

    private Optional( T obj ) {
        this.obj = obj;
        abs = false;
    }

    public T get() {
        return obj;
    }

    public boolean isPresent() {
        return ! abs;
    }

    @Override
    public boolean equals( final Object obj ) {
        if(!(obj instanceof Optional)) {
            return false;
        }
        Optional that = (Optional) obj;
        return that == EMPTY || (
                this.abs == that.abs && (
                        (this.obj == null && that.obj == null) || (
                                this.obj != null &&
                                that.obj != null &&
                                this.obj.equals( that.obj )
                        )
                )
        );
    }

    @Override
    public String toString() {
        return "Optional<" + (abs?"?":obj==null?"?":obj.getClass().getSimpleName()) + ">: present=" + !abs + ", value=(" + obj + ")";
    }
}
