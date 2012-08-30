package com.bazaarvoice.jolt.defaultr;

import java.util.Collection;
import java.util.Collections;

public class MapKey extends Key {

    public MapKey( String jsonKey ) {
        super.init( jsonKey );
    }

    public Collection getKeyValues() {
        return Collections.unmodifiableCollection(keyStrings);
    }

    @Override
    public int getLiteralIntKey() {
        throw new IllegalStateException( "Shouldn't be be asking a MapKey for int getLiteralIntKey()."  );
    }
}
