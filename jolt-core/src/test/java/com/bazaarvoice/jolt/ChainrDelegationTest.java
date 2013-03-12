package com.bazaarvoice.jolt;

import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import org.testng.AssertJUnit;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;

public class ChainrDelegationTest {

    @DataProvider
    public Object[][] badSpecs() throws IOException {
        return new Object[][] {
            {JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/bad_spec_arrayClassName.json" ) )},
            {JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/bad_spec_ClassName.json" ) )},
            {JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/bad_spec_NonTransformClass.json" ) )},
            {JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/bad_spec_empty.json" ) )},
            {JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/bad_spec_SpecTransform.json" ) )}
        };
    }

    @Test(dataProvider = "badSpecs", expectedExceptions = SpecException.class )
    public void testBadSpecs(Object chainrSpec) {
        new Chainr( chainrSpec ); // should fail when parsing spec
        AssertJUnit.fail( "Should not have gotten here" );
    }

    @DataProvider
    public Object[][] badTransforms() throws IOException {
        return new Object[][] {
            {JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/bad_transform_loadsExplodingDelegate.json" ) )}
        };
    }

    @Test(dataProvider = "badTransforms", expectedExceptions = TransformException.class )
    public void testBadTransforms(Object chainrSpec) {
        Chainr unit = new Chainr( chainrSpec );
        unit.transform( new HashMap() );// should fail here
        AssertJUnit.fail( "Should not have gotten here" );
    }

    @DataProvider
    public Object[][] passingTestCases() throws IOException {
        return new Object[][] {
            {new Object(), JsonUtils.jsonToObject( ChainrDelegationTest.class.getResourceAsStream( "/json/chainrDelegation/loadsGoodDelegate.json" ) )}
        };
    }

    @Test(dataProvider = "passingTestCases" )
    public void testPassing(Object input, Object spec) {
        Chainr unit = new Chainr( spec );
        DelegationResult actual = null;
        actual = (DelegationResult) unit.transform( input );

        AssertJUnit.assertEquals( input, actual.input );
        AssertJUnit.assertNotNull( actual.spec );
    }
}

class ExplodingDelegate implements Transform {

    @Override
    public Object transform( Object input ) {
        throw new TransformException( "kaboom" );
    }
}

class GoodDelegate implements SpecTransform {

    private Object spec;

    public GoodDelegate( Object spec ) {
        this.spec = spec;
    }

    @Override
    public Object transform( Object input ) {
        return new DelegationResult( input, spec );
    }
}

/**
 * Chainr should barf on this class, as it is a SpecTransform without a single arg constructor.
 */
class BadSpecTransform implements SpecTransform {

    @Override
    public Object transform( Object input ) {
        return input;
    }
}

class DelegationResult {
    Object input;
    Object spec;
    DelegationResult(Object input, Object spec) {
        this.input = input;
        this.spec = spec;
    }
}
