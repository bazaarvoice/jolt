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
package com.bazaarvoice.jolt.utils;

import com.beust.jcommander.internal.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Iterator;
import java.util.List;

/**
* StringTools Tester.
*/
public class StringToolsTest {

    @DataProvider (parallel = true)
    public Iterator<Object[]> testCaseGenerator() {
        List<Object[]> testCases = Lists.newArrayList();

        testCases.add(new String[] {null, null});
        testCases.add(new String[] {"", ""});

        testCases.add(new String[] {null, ""});
        testCases.add(new String[] {"", null});

        testCases.add(new String[] {RandomStringUtils.randomAscii(1<<2), null});
        testCases.add(new String[] {RandomStringUtils.randomAscii(1<<2), ""});

        testCases.add(new String[] {null, RandomStringUtils.randomAscii(1<<2)});
        testCases.add(new String[] {"", RandomStringUtils.randomAscii(1<<2)});


        int i=1<<6;
        while(i-- > 0) {
            testCases.add(new String[] { RandomStringUtils.randomAscii(1<<10), RandomStringUtils.randomAscii(1<<2) });
            testCases.add(new String[] { RandomStringUtils.randomAscii(1<<2), RandomStringUtils.randomAscii(1<<10) });

            testCases.add(new String[] { RandomStringUtils.randomAlphabetic(1<<10), RandomStringUtils.randomAlphabetic(1<<2) });
            testCases.add(new String[] { RandomStringUtils.randomAlphabetic(1<<2), RandomStringUtils.randomAlphabetic(1<<10) });

            testCases.add(new String[] { RandomStringUtils.randomAlphanumeric(1<<10), RandomStringUtils.randomAlphanumeric(1<<2) });
            testCases.add(new String[] { RandomStringUtils.randomAlphanumeric(1<<2), RandomStringUtils.randomAlphanumeric(1<<10) });
        }

        return testCases.iterator();
    }

    @Test(dataProvider = "testCaseGenerator")
    public void testCountMatches(String str, String subStr) throws Exception {

        Assert.assertEquals(
                StringTools.countMatches(str, subStr),
                StringTools.countMatches(str, subStr),
                "test failed: \nstr=\"" + str + "\"\nsubStr=\"" + subStr + "\""
        );
    }

    @Test
    public void testIsNotBlank() throws Exception {
        Assert.assertTrue(StringTools.isNotBlank(" a a "));
        Assert.assertTrue(StringTools.isNotBlank("a a"));
        Assert.assertTrue(StringTools.isNotBlank(" a "));
        Assert.assertTrue(StringTools.isNotBlank("a"));

        Assert.assertFalse(StringTools.isNotBlank("  "));
        Assert.assertFalse(StringTools.isNotBlank(" "));
        Assert.assertFalse(StringTools.isNotBlank(""));
        Assert.assertFalse(StringTools.isNotBlank(null));
    }

    @Test
    public void testIsBlank() throws Exception {
        Assert.assertFalse(StringTools.isBlank(" a a "));
        Assert.assertFalse(StringTools.isBlank("a a"));
        Assert.assertFalse(StringTools.isBlank(" a "));
        Assert.assertFalse(StringTools.isBlank("a"));

        Assert.assertTrue(StringTools.isBlank("  "));
        Assert.assertTrue(StringTools.isBlank(" "));
        Assert.assertTrue(StringTools.isBlank(""));
        Assert.assertTrue(StringTools.isBlank(null));
    }

    @Test
    public void testIsEmpty() throws Exception {
        Assert.assertTrue(StringTools.isEmpty(""));
        Assert.assertTrue(StringTools.isEmpty(null));
        Assert.assertFalse(StringTools.isEmpty(" "));
    }
} 
