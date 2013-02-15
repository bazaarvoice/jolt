package com.bazaarvoice.jolt.traversr.traversal;

import com.bazaarvoice.jolt.traversr.Traversr;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TraversalStep that expects to handle Map objects.
 */
public class MapTraversalStep extends BaseTraversalStep<Map<String,Object>> {

    public MapTraversalStep( Traversr traversr, TraversalStep child ) {
        super( traversr, child );
    }

    public Class getStepType() {
        return Map.class;
    }

    public Object newContainer() {
        return new LinkedHashMap<String, Object>();
    }

    @Override
    public Object get( Map<String, Object> map, String key ) {
        return map.get( key );
    }

    @Override
    public Object remove( Map<String, Object> map, String key ) {
        return map.remove( key );
    }

    @Override
    public Object overwriteSet( Map<String, Object> map, String key, Object data ) {
        map.put(  key, data );
        return data;
    }
}
