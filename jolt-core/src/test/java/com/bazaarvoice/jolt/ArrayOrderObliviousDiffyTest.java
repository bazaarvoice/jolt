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

import com.beust.jcommander.internal.Lists;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

public class ArrayOrderObliviousDiffyTest {

    ArrayOrderObliviousDiffy unit;

    @BeforeClass
    public void before() throws Exception {
        unit = new ArrayOrderObliviousDiffy();
    }

    @AfterClass
    public void after() throws Exception {
        unit = null;
    }

    @DataProvider(parallel = true)
    public Iterator<Object[]> testCases() {
        List<Object[]> testCases = Lists.newArrayList();
        testCases.add(new Object[] {"arrayOrderObliviousDiffy/bugFix95"}); // see https://github.com/bazaarvoice/jolt/issues/95
        testCases.add(new Object[] {"arrayOrderObliviousDiffy/simpleCase"});
        testCases.add(new Object[] {"arrayOrderObliviousDiffy/complexCase"});
        return testCases.iterator();
    }

    @Test(dataProvider = "testCases")
    public void ArrayOrderObliviousDiffy(String testCase) throws Exception {
        Object expected = JsonUtils.classpathToObject("/jsonUtils/" + testCase + "/expected.json");
        Object actual = JsonUtils.classpathToObject("/jsonUtils/" + testCase + "/actual.json");
        Diffy.Result result = unit.diff(expected, actual);
        Assert.assertTrue(result.isEmpty(), result.toString());
    }
} 
