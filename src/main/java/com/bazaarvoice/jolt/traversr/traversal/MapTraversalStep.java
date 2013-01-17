package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.traversr.Traversr;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapTraversalStep extends BaseTraversalStep<Map<String,Object>> {

    public MapTraversalStep( Traversr traversr, TraversalStep child ) {
        super( traversr, child );
    }

    public boolean typeOk( Object tree ) {
        return tree instanceof Map;
    }

    public Object newContainer() {
        return new LinkedHashMap<String, Object>();
    }

    @Override
    public Object doGet( Map<String, Object> map, String key ) {
        return map.get( key );
    }

    @Override
    public Object doOverwriteSet( Map<String, Object> map, String key, Object data ) {
        map.put(  key, data );
        return data;
    }
}
