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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Map;

public class ShiftrTest {

    // TODO: test arrays better (wildcards test array could be in reverse order)
    @DataProvider
    public Object[][] getTestCaseUnits() {
        return new Object[][] {
            {"arrayExample"},
            {"arrayMismatch"},
            {"bucketToPrefixSoup"},
            {"declaredOutputArray"},
            {"escapeAllTheThings"},
            {"escapeAllTheThings2"},
            {"explicitArrayKey"},
            {"filterParallelArrays"},
            {"filterParents1"},
            {"filterParents2"},
            {"filterParents3"},
            {"firstSample"},
            {"hashDefault"},
            {"identity"},
            {"inputArrayToPrefix"},
            {"invertMap"},
            {"json-ld-escaping"},
            {"keyref"},
            {"lhsAmpMatch"},
            {"listKeys"},
            {"mapToList"},
            {"mapToList2"},
            {"mergeParallelArrays1_and-transpose"},
            {"mergeParallelArrays2_and-do-not-transpose"},
            {"mergeParallelArrays3_and-filter"},
            {"multiPlacement"},
            {"objectToArray"},
            {"passNullThru"},
            {"passThru"},
            {"pollaxman_218_duplicate_speclines_bug"},
            {"prefixDataToArray"},
            {"prefixedData"},
            {"prefixSoupToBuckets"},
            {"queryMappingXform"},
            {"shiftToTrash"},
            {"simpleLHSEscape"},
            {"simpleRHSEscape"},
            {"singlePlacement"},
            {"specialKeys"},
            {"transposeArrayContents1"},
            {"transposeArrayContents2"},
            {"transposeComplex1"},
            {"transposeComplex2"},
            {"transposeComplex3_both-sides-multipart"},
            {"transposeComplex4_lhs-multipart-rhs-sugar"},
            {"transposeComplex5_at-logic-with-embedded-array-lookups"},
            {"transposeComplex6_rhs-complex-at"},
            {"transposeComplex7_coerce-int-string-conversion"},
            {"transposeComplex8_coerce-boolean-string-conversion"},
            {"transposeComplex9_lookup_an_array_index"},
            {"transposeInverseMap1"},
            {"transposeInverseMap2"},
            {"transposeLHS1"},
            {"transposeLHS2"},
            {"transposeLHS3"},
            {"transposeNestedLookup"},
            {"transposeSimple1"},
            {"transposeSimple2"},
            {"transposeSimple3"},
            {"wildcards"},
            {"wildcardSelfAndRef"},
            {"wildcardsWithOr"}
        };
    }

    // TODO: test arrays better (wildcards test array could be in reverse order)

    @Test(dataProvider = "getTestCaseUnits")
    public void runTestUnits(String testCaseName) throws IOException {

        String testPath = "/json/shiftr/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath + ".json" );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Shiftr shiftr = new Shiftr( spec );
        Object actual = shiftr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }
}
