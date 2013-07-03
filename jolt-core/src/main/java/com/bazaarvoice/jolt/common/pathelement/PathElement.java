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
package com.bazaarvoice.jolt.common.pathelement;

public interface PathElement {

    String getRawKey();

    /**
     * Get the canonical form of this PathElement.  Really only interesting for the Reference Path element, where
     *  it will expand "&" to "&0(0)".
     * @return canonical String version of this PathElement
     */
    String getCanonicalForm();
}
