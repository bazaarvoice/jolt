/*
 * Copyright 2013-2023 Bazaarvoice, Inc.
 * Copyright 2025 Jolt Community
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
package io.joltcommunity.jolt.common.reference;


/**
 * Reference is used by Shiftr when lookup up values from a WalkedPath (list of LiteralPathElements).
 * <p>
 * #,  #0  are the same
 * <p>
 * The "canonical form" is "Cx", where :
 * <pre>
 * C : the character used to determine the type of Reference
 * x : pathIndex : which is how far up the walkedPath the look
 * </pre>
 */
public interface PathReference {

    int getPathIndex();

    /**
     * Get the canonical form of this Reference.
     * <p>
     * One of the uses of this method is to ensure that spec, does not contain "duplicate" keys, aka
     * two keys that when you unroll the syntactic sugar, are the same thing.
     *
     * @return fully expanded String representation of this Reference
     */
    String getCanonicalForm();
}
