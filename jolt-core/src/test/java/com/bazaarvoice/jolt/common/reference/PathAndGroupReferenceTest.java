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
package com.bazaarvoice.jolt.common.reference;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PathAndGroupReferenceTest {

    @DataProvider
    public Object[][] getValidReferenceTests() {
        return new Object[][] {
            {     "", 0, 0, "(0,0)" },
            {    "3", 3, 0, "(3,0)" },
            {  "(3)", 3, 0, "(3,0)" },
            {"(1,2)", 1, 2, "(1,2)" }
        };
    }

    @Test( dataProvider = "getValidReferenceTests" )
    public void validAmpReferencePatternTest(String key, int pathIndex, int keyGroup, String canonicalForm) {

        PathAndGroupReference amp = new AmpReference( "&" + key );
        Assert.assertEquals( pathIndex, amp.getPathIndex() );
        Assert.assertEquals( keyGroup, amp.getKeyGroup() );
        Assert.assertEquals( "&" + canonicalForm, amp.getCanonicalForm() );
    }

    @Test( dataProvider = "getValidReferenceTests" )
    public void validDollarReferencePatternTest(String key, int pathIndex, int keyGroup, String canonicalForm) {

        PathAndGroupReference amp = new DollarReference( "$" + key );
        Assert.assertEquals( pathIndex, amp.getPathIndex() );
        Assert.assertEquals( keyGroup, amp.getKeyGroup() );
        Assert.assertEquals( "$" + canonicalForm, amp.getCanonicalForm() );
    }


    @DataProvider
    public Object[][] getFailReferenceTests() {
        return new Object[][] {
            { "pants" },
            { "-1" },
            { "(-1,2)" },
            { "(1,-2)" },
        };
    }

    @Test( dataProvider = "getFailReferenceTests", expectedExceptions = SpecException.class  )
    public void failAmpReferencePatternTest(String key ) {
        new AmpReference( "&" + key );
    }

    @Test( dataProvider = "getFailReferenceTests", expectedExceptions = SpecException.class )
    public void failDollarReferencePatternTest(String key ) {
        new DollarReference( "$" + key );
    }
}
