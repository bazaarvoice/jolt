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
package com.bazaarvoice.jolt;

/**
 * Marker interface for all Jolt Transforms.
 *
 * Jolt Transforms should not actually implement this interface.  Instead they should
 * implement either the Transform interface or the ContextualTransform interface.
 *
 * This interface exists because the Transform and ContextualTransform interfaces do not
 * share any methods, but we need a need a way to flag a class being a JoltTransform.
 */
public interface JoltTransform {
}
