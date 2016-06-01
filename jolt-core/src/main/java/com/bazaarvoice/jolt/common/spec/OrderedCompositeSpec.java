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

package com.bazaarvoice.jolt.common.spec;

import com.bazaarvoice.jolt.common.ExecutionStrategy;

import java.util.List;
import java.util.Map;

/**
 * An ordered composite spec denotes the spec will have Literal and Computed children that
 * must be Ordered Spec, which should be subject to sorting to before applying any of the
 * determined execution strategies!
 *
 * This is not enforced directly, but these interface methods ensure the executionStrategy
 * gets the literal and computed children lists to process its exec strategy
 *
 * The order is provided by a Map<Class, int> and then ordering is achieved using a comparator
 */
public interface OrderedCompositeSpec extends BaseSpec {

    Map<String, ? extends BaseSpec> getLiteralChildren();

    List<? extends BaseSpec> getComputedChildren();

    ExecutionStrategy determineExecutionStrategy();
}
