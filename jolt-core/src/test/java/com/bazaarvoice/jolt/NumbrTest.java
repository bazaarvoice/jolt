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

import com.bazaarvoice.jolt.numbr.MatcherPredicate;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class NumbrTest {

    @DataProvider
    public Object[][] getTestCaseNames() {
        return new Object[][] {
                {"literalMatch"},
                {"wildcardMatch"},
                {"orMatch"},
                {"arrayMatch"},
                {"objectMatch"},
                {"incorrectTypeMatch"},
        };
    }

    @Test(dataProvider = "getTestCaseNames")
    public void runTestCases(String testCaseName) throws IOException {

        String testPath = "/json/numbr/" + testCaseName;
        Map<String, Object> testUnit = JsonUtils.classpathToMap( testPath + ".json" );

        Object input = testUnit.get( "input" );
        Object spec = testUnit.get( "spec" );
        Object expected = testUnit.get( "expected" );

        Numbr numbr = new Numbr( spec );
        Object actual = numbr.transform( input );

        JoltTestUtil.runDiffy( "failed case " + testPath, expected, actual );
    }

    @Test
    public void testMatcherPredicatePrecedence() {
        MatcherPredicate star = MatcherPredicate.extractMatcherPredicates("*").get(0);
        MatcherPredicate appleStar = MatcherPredicate.extractMatcherPredicates("apple-*").get(0);
        MatcherPredicate zebraStar = MatcherPredicate.extractMatcherPredicates("zebra-*").get(0);
        MatcherPredicate apple = MatcherPredicate.extractMatcherPredicates("apple").get(0);
        MatcherPredicate zebra = MatcherPredicate.extractMatcherPredicates("zebra").get(0);

        // Intentionally inserted out-of-order
        MatcherPredicate[] actual = { star, zebraStar, apple, zebra, appleStar };
        Arrays.sort(actual);
        MatcherPredicate[] expected = { apple, zebra, appleStar, zebraStar, star };

        assertEquals(actual, expected);
    }
}
