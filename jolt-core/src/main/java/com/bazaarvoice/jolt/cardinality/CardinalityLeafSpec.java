package com.bazaarvoice.jolt.cardinality;

import com.bazaarvoice.jolt.common.WalkedPath;
import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.exception.SpecException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Leaf level CardinalitySpec object.
 * <p/>
 * If this CardinalitySpec's PathElement matches the input (successful parallel tree walk)
 * this CardinalitySpec has the information needed to write the given data to the output object.
 */
public class CardinalityLeafSpec extends CardinalitySpec {

    public enum CardinalityRelationship {
        ONE,
        MANY
    }

    private CardinalityRelationship cardinalityRelationship;

    public CardinalityLeafSpec( String rawKey, Object rhs ) {
        super( rawKey );

        try {
            cardinalityRelationship = CardinalityRelationship.valueOf( rhs.toString() );
        }
        catch( Exception e ) {
            throw new SpecException( "Invalid Cardinality type :" + rhs.toString(), e );
        }
    }

    /**
     * If this CardinalitySpec matches the inputkey, then do the work of modifying the data and return true.
     *
     * @return true if this this spec "handles" the inputkey such that no sibling specs need to see it
     */
    @Override
    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer ) {

        LiteralPathElement thisLevel = getMatch( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }
        performCardinalityAdjustment( inputKey, input, walkedPath, (Map) parentContainer, thisLevel );
        return true;
    }

    /**
     * This should only be used by composite specs with an '@' child
     *
     * @return null if no work was done, otherwise returns the re-parented data
     */
    public Object applyToParentContainer ( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer ) {

        LiteralPathElement thisLevel = getMatch( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return null;
        }
        return performCardinalityAdjustment( inputKey, input, walkedPath, (Map) parentContainer, thisLevel );
    }

    /**
     *
     * @return null if no work was done, otherwise returns the re-parented data
     */
    private Object performCardinalityAdjustment( String inputKey, Object input, WalkedPath walkedPath, Map parentContainer, LiteralPathElement thisLevel ) {

        // Add our the LiteralPathElement for this level, so that write path References can use it as &(0,0)
        walkedPath.add( thisLevel );

        Object returnValue = null;
        if ( cardinalityRelationship == CardinalityRelationship.MANY ) {
            if ( input instanceof List ) {
                returnValue = input;
            }
            else if ( input instanceof Map || input instanceof String || input instanceof Number || input instanceof Boolean ) {
                Object one = parentContainer.remove( inputKey );
                returnValue = new ArrayList<Object>();
                ( (List) returnValue ).add( one );
            }
            else if ( input == null ) {
                returnValue = Collections.emptyList();
            }
            parentContainer.put( inputKey, returnValue );
        }
        else if ( cardinalityRelationship == CardinalityRelationship.ONE ) {
            if ( input instanceof List ) {
                if (!( (List) input ).isEmpty()) {
                    returnValue = ( (List) input ).get( 0 );
                }
                parentContainer.put( inputKey, returnValue );
            }
        }

        walkedPath.removeLast();

        return returnValue;
    }

    private LiteralPathElement getMatch( String inputKey, WalkedPath walkedPath ) {
        return pathElement.match( inputKey, walkedPath );
    }
}
