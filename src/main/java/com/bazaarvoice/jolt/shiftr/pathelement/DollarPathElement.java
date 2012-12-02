package com.bazaarvoice.jolt.shiftr.pathelement;

import com.bazaarvoice.jolt.shiftr.reference.DollarReference;
import com.bazaarvoice.jolt.shiftr.WalkedPath;

public class DollarPathElement extends PathElement {

    private final DollarReference dRef;

    public DollarPathElement( String key ) {
        super(key);

        dRef = new DollarReference( key );
    }

    @Override
    public String getCanonicalForm() {
        return dRef.getCanonicalForm();
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {
        LiteralPathElement pe = walkedPath.elementFromEnd( dRef.getPathIndex() );
        return pe.getSubKeyRef( dRef.getKeyGroup() );
    }

    @Override
    public LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        String evaled = evaluate( walkedPath );
        return new LiteralPathElement( evaled );
    }
}
