package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.common.WalkedPath;

public class AtPathElement extends BasePathElement implements MatchablePathElement {
    public AtPathElement( String key ) {
        super(key);

        if ( ! "@".equals( key ) ) {
            throw new SpecException( "'References Input' key '@', can only be a single '@'.  Offending key : " + key );
        }
    }

    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        return walkedPath.lastElement();  // copy what our parent was so that write keys of &0 and &1 both work.
    }

    @Override
    public String getCanonicalForm() {
        return "@";
    }
}
