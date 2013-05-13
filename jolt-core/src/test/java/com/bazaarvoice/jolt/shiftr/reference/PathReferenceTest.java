package com.bazaarvoice.jolt.shiftr.reference;

import com.bazaarvoice.jolt.exception.SpecException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class PathReferenceTest {

    @DataProvider
    public Object[][] getValidReferenceTests() {
        return new Object[][] {
            {     "", 0, "0" },
            {    "3", 3, "3" },
            {  "12", 12, "12" }
        };
    }

    @Test( dataProvider = "getValidReferenceTests" )
    public void validAmpReferencePatternTest(String key, int pathIndex, String canonicalForm) {

        PathReference ref = new HashReference( "#" + key );
        AssertJUnit.assertEquals( pathIndex, ref.getPathIndex() );
        AssertJUnit.assertEquals( "#" + canonicalForm, ref.getCanonicalForm() );
    }


    @DataProvider
    public Object[][] getFailReferenceTests() {
        return new Object[][] {
            { "pants" },
            { "-1" },
            { "(1)" }
        };
    }

    @Test( dataProvider = "getFailReferenceTests", expectedExceptions = SpecException.class  )
    public void failAmpReferencePatternTest(String key ) {
        new HashReference( "#" + key );
    }
}
