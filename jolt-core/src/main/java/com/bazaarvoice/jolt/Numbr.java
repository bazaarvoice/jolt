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

import com.bazaarvoice.jolt.numbr.ArraySpec;
import com.bazaarvoice.jolt.numbr.MapSpec;
import com.bazaarvoice.jolt.numbr.MatcherPredicate;
import com.bazaarvoice.jolt.numbr.Spec;
import com.bazaarvoice.jolt.numbr.ValueSpec;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Numbr is a JOLT transform that converts numeric data from one type to another type.  For example, assume that a JSON
 * object document can contain attribute called "rank" which can be a double on a numeric scale of 0.0 to 10.0.  When Jackson
 * reads a document it applies the closest numeric type based on the incoming data.  So, for example, the following two documents
 * will contain different value types when parsed by Jackson:
 *
 *     {"rank", 6.5}
 *     // Deserializes to HashMap of {"rank", new Double(6.5)}
 *
 *     {"rank", 9}
 *     // Deserializes to HashMap of {"rank", new Integer(9)}
 *
 * Most of the time this is a non-issue.  However, some systems, particularly those that perform schema validation, will raise an
 * exception if the schema defines "rank" as a double but an integer value is found.
 *
 * Numbr takes a spec which explicitly describes the numberic type for each field.  For example, a Numbr with the following spec
 * would ensure that "rank" in the examples above always has a Double value:
 *
 *     {"rank": "double"}
 *
 * When defining the spec leaf nodes contain the desired type for the value at that location.  Valid values are "int", "long", "float",
 * and "double".  All non-leaf nodes describe how to match sub-trees.  If at any point no match is found or if a leaf-node match is found
 * that is not a number then the value is unchanged in the result.
 *
 * The matcher types supported are:
 *
 * - Literal match
 *
 * In the simplest case an exact match of the provided key is used to transform the value:
 *
 * Example:
 *     Spec:
 *         {"rank":"double", "size","integer"}
 *
 *     Transformation:
 *         {"rank": 7, "size": 3.2, "ignore": 42}
 *         becomes
 *         {"rank": 7.0, "size": 3, "ignore: 42}
 *
 * - Wildcard match
 *
 * Similar to the literal match except that "*" can be used as a wildcard in the key.  A value of exactly "*" will replace
 * any direct numeric children to the desired type.  Note that this is not transitive beyond immediate children.
 *
 * Example:
 *     Spec:
 *         {"rank-*": "double"}
 *
 *     Transformation:
 *         {"rank-fit": 3, "rank-color": 8, "rank-parent": {"rank-unchanged": 1}}
 *         becomes
 *         {"rank-fit": 3.0, "rank-color": 8.0, "rank-parent": {"rank-unchanged": 1}}
 *
 * - Or match
 *
 * Similar to literal and wildcard matches except multiple values can be separated with a "|"
 *
 * Example:
 *     Spec:
 *         {"lat|long|rank-*": "double"}
 *
 *     Transformation:
 *         {"lat": 30, "long": 60, "rank-size": 5, "ignore": 1}
 *         becomes
 *         {"lat": 30.0, "long": 60.0, "rank-size": 5.0, "ignore": 1}
 *
 * - Array
 *
 * To traverse values of an array use a key with a suffix of "[]".  Note that the subsequent transform will be applied
 * to ALL array values and that if the matching value is not an array then no transform will take place:
 *
 * Example:
 *     Spec:
 *         {"sizes[]": "double"}
 *
 *     Transformation:
 *         {"sizes":[1, 2, 3.0], "ignore": 1}
 *         becomes
 *         {"sizes":[1.0, 2.0, 3.0], "ignore": 1}
 *
 *
 * The above transforms can be nested in the spec as needed:
 *
 *     Spec:
 *         {"geoLocation": {"lat|long":"double"}}
 *
 *     Transformation:
 *         {"name":"Mary", "geoLocation":{"lat":30,"long":60}, "ignore": 1}
 *         becomes
 *         {"name":"Mary", "geoLocation":{"lat":30.0,"long":60.0}, "ignore": 1}
 *
 */
public class Numbr implements SpecDriven, Transform {

    private final Spec _rootSpec;

    @Inject
    public Numbr(Object spec) {
        _rootSpec = buildSpec(spec);
    }

    private Spec buildSpec(Object spec) {
        if (spec instanceof Map) {
            //noinspection unchecked
            Map<String, Object> map = (Map<String, Object>) spec;

            LinkedHashMap<MatcherPredicate, Spec> mapSpecs = new LinkedHashMap<>();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String matcherString = entry.getKey();
                List<MatcherPredicate> matchers;
                Spec childSpec;

                if (matcherString.endsWith(ArraySpec.SPEC_SUFFIX)) {
                    // Key indicates an array
                    String arrayKey = matcherString.substring(0, matcherString.length() - ArraySpec.SPEC_SUFFIX.length());
                    matchers = MatcherPredicate.extractMatcherPredicates(arrayKey);
                    childSpec = new ArraySpec(buildSpec(entry.getValue()));
                } else {
                    // Only other supported option is a nested map
                    matchers = MatcherPredicate.extractMatcherPredicates(matcherString);
                    childSpec = buildSpec(entry.getValue());
                }

                for (MatcherPredicate matcher : matchers) {
                    mapSpecs.put(matcher, childSpec);
                }
            }

            return new MapSpec(mapSpecs);
        }

        if (spec instanceof String) {
            return ValueSpec.forType((String) spec);
        }

        throw new IllegalArgumentException("Unsupported type in spec: " + spec.getClass().getName());
    }

    @Override
    public Object transform(Object input) {
        return _rootSpec.transform(input);
    }
}
