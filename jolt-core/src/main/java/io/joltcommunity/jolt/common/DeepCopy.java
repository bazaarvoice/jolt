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
package io.joltcommunity.jolt.common;

import java.io.*;

public class DeepCopy {

    /**
     * Simple deep copy, that leverages Java Serialization.
     * Supplied object is serialized to an in memory buffer (byte array),
     * and then a new object is reconstituted from that byte array.
     * <p>
     * This is meant for copying small objects or object graphs, and will
     * probably do nasty things if asked to copy a large graph.
     *
     * @param object object to deep copy
     * @return deep copy of the object
     */
    public static Object simpleDeepCopy(Object object) {
        try (
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(object);
            oos.flush();
            byte[] byteData = bos.toByteArray();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
                 ObjectInputStream ois = new ObjectInputStream(bais)) {
                return ois.readObject();
            }
        } catch (IOException | ClassNotFoundException ex) {
            throw new RuntimeException("DeepCopy failed", ex);
        }
    }
}
