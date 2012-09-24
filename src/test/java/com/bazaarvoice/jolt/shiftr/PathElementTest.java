package com.bazaarvoice.jolt.shiftr;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

public class PathElementTest {

    @Test
    public void referenceTest() {

        Path<PathElement> path = Path.parseDotNotation( "SecondaryRatings.tuna-&0(1)-marlin.Value" );

        AssertJUnit.assertEquals( "SecondaryRatings", path.elementAt(0).rawKey );
        AssertJUnit.assertEquals( "SecondaryRatings", path.elementAt(0).toString() );
        AssertJUnit.assertEquals( "Value", path.elementFromEnd(0).rawKey );
        AssertJUnit.assertEquals( "Value", path.elementFromEnd( 0 ).toString() );
        AssertJUnit.assertEquals( "Value", path.lastElement().toString() );

        PathElement.ReferencePathElement refElement = (PathElement.ReferencePathElement) path.elementAt(1);

        AssertJUnit.assertEquals( 3, refElement.tokens.size() );
        AssertJUnit.assertEquals( "tuna-", (String) refElement.tokens.get(0) );
        AssertJUnit.assertEquals( "-marlin", (String) refElement.tokens.get(2) );

        AssertJUnit.assertTrue( refElement.tokens.get(1) instanceof Reference );
        Reference ref = (Reference) refElement.tokens.get(1);
        AssertJUnit.assertFalse( ref.isArray );
        AssertJUnit.assertEquals( 0, ref.pathIndex );
        AssertJUnit.assertEquals( 1, ref.keyGroup );
    }

    @Test
    public void arrayRefTest() {

        Path<PathElement> path = Path.parseDotNotation( "ugc.photos-&1-[&(1)]" );

        PathElement.ReferencePathElement refElement = (PathElement.ReferencePathElement) path.elementAt(1);

        AssertJUnit.assertEquals( 4, refElement.tokens.size() );

        {
            AssertJUnit.assertTrue( refElement.tokens.get(0) instanceof String );
            AssertJUnit.assertEquals( "photos-", (String) refElement.tokens.get(0) );
        }
        {
            AssertJUnit.assertTrue( refElement.tokens.get(1) instanceof Reference );
            Reference ref = (Reference) refElement.tokens.get(1);
            AssertJUnit.assertFalse( ref.isArray );
            AssertJUnit.assertEquals( 1, ref.pathIndex );
            AssertJUnit.assertEquals( 0, ref.keyGroup );
        }
        {
            AssertJUnit.assertTrue( refElement.tokens.get(2) instanceof String );
            AssertJUnit.assertEquals( "-", (String) refElement.tokens.get(2) );
        }
        {
            AssertJUnit.assertTrue( refElement.tokens.get(3) instanceof Reference );
            Reference ref = (Reference) refElement.tokens.get(3);
            AssertJUnit.assertTrue( ref.isArray );
            AssertJUnit.assertEquals( 0, ref.pathIndex );
            AssertJUnit.assertEquals( 1, ref.keyGroup );
        }
    }
}
