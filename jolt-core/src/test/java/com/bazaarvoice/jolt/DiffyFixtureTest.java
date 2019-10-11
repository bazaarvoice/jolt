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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class DiffyFixtureTest {

    Diffy diffy = new Diffy();
    ArrayOrderObliviousDiffy aooDiffy = new ArrayOrderObliviousDiffy();

    @DataProvider(parallel = true)
    public Object[][] diffyWhenThingsDontMatchTestCases() {
        return new Object[][] {
                // for this test, there is only one thing different, and both Diffies return the same diff
                {diffy,    "esQuery1", "expectedDiff"},
                {aooDiffy, "esQuery1", "expectedDiff"},

                // in this test, the same thing from above is different, but the order of things has been scrabled.
                // thus the aooDiff is way smaller than the base Diffy
                {diffy,    "esQuery2", "expectedDiff"},
                {aooDiffy, "esQuery2", "expectedAOODiff"},

                {diffy,    "differentSizedLists", "expectedDiff"},
                {aooDiffy, "differentSizedLists", "expectedAOODiff"},
        };
    }

    /**
     * So this test a little bit Meta.
     *
     * The idea is, we want to test what Diffy returns when the inputs to Diffy do not match.
     *
     * So, run Diffy with inputs "A" and "B", and then compare the result against an expected Diff "C".
     *
     * However Diffy "ignores" nulls in the inputs and expected data, thus we do a base level Map.equals().
     */
    @Test(dataProvider = "diffyWhenThingsDontMatchTestCases")
    public void testDiffyWhenThingsDontMatch(Diffy diffy, String testCase, String expectedFile) throws Exception {

        Object testActual =   JsonUtils.classpathToObject("/jsonUtils/diffyWhenDifferent/" + testCase + "/testActual.json");
        Object testExpected = JsonUtils.classpathToObject("/jsonUtils/diffyWhenDifferent/" + testCase + "/testExpected.json");

        Map expectedDiff = JsonUtils.classpathToMap("/jsonUtils/diffyWhenDifferent/" + testCase + "/" + expectedFile + ".json");

        Diffy.Result testResult = diffy.diff( testExpected, testActual );
        Assert.assertFalse( testResult.isEmpty(), "Test diffs match when the shouldn't.");

        //  expectedDiff.equals cause the Map.equals is deep and everything must be in order.
        Assert.assertTrue( expectedDiff.equals( testResult.expected ), "The meta diff was not empty, when it should have.");
    }
} 
