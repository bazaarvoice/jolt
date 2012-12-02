package com.bazaarvoice.jolt.shiftr.pathelement;

import com.bazaarvoice.jolt.shiftr.WalkedPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LiteralPathElement extends PathElement {

    private final List<String> subKeys;

    public LiteralPathElement( String key ) {
        super(key);

        List<String> keys = new ArrayList<String>(1);
        keys.add( key ); // always add the full key to index 0

        this.subKeys = Collections.unmodifiableList( keys );
    }

    public LiteralPathElement( String key, List<String> subKeys ) {
        super(key);

        if ( subKeys == null ) {
            throw new IllegalArgumentException( "LiteralPathElement for key:" + key + " got null list of subKeys" );
        }

        List<String> keys = new ArrayList<String>( 1 + subKeys.size() );
        keys.add( key ); // always add the full key to index 0
        keys.addAll( subKeys );

        this.subKeys = Collections.unmodifiableList( keys );
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {
        return getRawKey();
    }

    @Override
    public com.bazaarvoice.jolt.shiftr.pathelement.LiteralPathElement match( String dataKey, WalkedPath walkedPath ) {
        return getRawKey().equals( dataKey ) ? this : null ;
    }

    @Override
    public String getCanonicalForm() {
        return getRawKey();
    }

    public String getSubKeyRef( int index ) {
        if ((index < 0) || (index >= this.subKeys.size())) {
            throw new IndexOutOfBoundsException( "LiteralPathElement "+ this.subKeys +" cannot be indexed with index "+index );
        }
        return subKeys.get( index );
    }

    public int getSubKeyCount(){
        return subKeys.size();
    }
}
