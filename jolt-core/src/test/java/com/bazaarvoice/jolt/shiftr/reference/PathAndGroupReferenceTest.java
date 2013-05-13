package com.bazaarvoice.jolt.shiftr.reference;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.AssertJUnit;
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
        AssertJUnit.assertEquals( pathIndex, amp.getPathIndex() );
        AssertJUnit.assertEquals( keyGroup, amp.getKeyGroup() );
        AssertJUnit.assertEquals( "&" + canonicalForm, amp.getCanonicalForm() );
    }

    @Test( dataProvider = "getValidReferenceTests" )
    public void validDollarReferencePatternTest(String key, int pathIndex, int keyGroup, String canonicalForm) {

        PathAndGroupReference amp = new DollarReference( "$" + key );
        AssertJUnit.assertEquals( pathIndex, amp.getPathIndex() );
        AssertJUnit.assertEquals( keyGroup, amp.getKeyGroup() );
        AssertJUnit.assertEquals( "$" + canonicalForm, amp.getCanonicalForm() );
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
