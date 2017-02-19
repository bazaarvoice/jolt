package com.bazaarvoice.jolt.callr;

import com.bazaarvoice.jolt.common.pathelement.*;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.utils.StringTools;

import java.util.Map;

public abstract class CallrSpec {

    protected static final String CLASS = "~class";
    protected static final String METHOD = "~method";
    protected static final String ARGS = "~args";

    private static final String STAR = "*";
    private static final String AT = "@";

    protected final MatchablePathElement pathElement;

    public CallrSpec( String rawJsonKey ) {
        PathElement pe = parse( rawJsonKey );
        if ( !( pe instanceof MatchablePathElement ) ) {
            throw new SpecException( "Spec LHS key=" + rawJsonKey + " is not a valid LHS key." );
        }
        this.pathElement = (MatchablePathElement) pe;
    }

    public static PathElement parse( String key ) {

        if ( key.contains( AT ) ) {
            return new AtPathElement( key );
        } else if ( STAR.equals( key ) ) {
            return new StarAllPathElement( key );
        } else if ( key.contains( STAR ) ) {
            if ( StringTools.countMatches( key, STAR ) == 1 ) {
                return new StarSinglePathElement( key );
            } else {
                return new StarRegexPathElement( key );
            }
        } else {
            return new LiteralPathElement( key );
        }
    }

    public boolean isLeaf( Map<String, Object> rhs ) {
        if ( !rhs.containsKey( CLASS ) || !rhs.containsKey( METHOD ) || !rhs.containsKey( ARGS ) ) {
            return false;
        }
        return true;
    }

    public abstract boolean apply( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer );
}
