/*
 * Copyright 2013 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bazaarvoice.jolt.chainr.spec;

import com.bazaarvoice.jolt.CardinalityTransform;
import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.Defaultr;
import com.bazaarvoice.jolt.Removr;
import com.bazaarvoice.jolt.Shiftr;
import com.bazaarvoice.jolt.Sortr;
import com.bazaarvoice.jolt.SpecTransform;
import com.bazaarvoice.jolt.Transform;
import com.bazaarvoice.jolt.exception.SpecException;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that encapsulates the information one of the individual transform entries in
 * the Chainr spec's list.
 *
 * If I didn't want to keep Jackson from being a dependency, this would be the type of class that
 * I would have Jackson load for me.
 */
public class ChainrEntry {

    /**
     * Map transform "operation" names to the classes that handle them
     */
    public static final Map<String, String> STOCK_TRANSFORMS;
    static {
        HashMap<String, String> temp = new HashMap<String, String>();
        temp.put( "shift", Shiftr.class.getCanonicalName() );
        temp.put( "default", Defaultr.class.getCanonicalName() );
        temp.put( "remove", Removr.class.getCanonicalName() );
        temp.put( "sort", Sortr.class.getCanonicalName() );
        temp.put( "cardinality", CardinalityTransform.class.getCanonicalName() );
        STOCK_TRANSFORMS = Collections.unmodifiableMap( temp );
    }

    public static final String OPERATION_KEY = "operation";
    public static final String SPEC_KEY = "spec";

    private final int index;
    private final Object spec;
    private final String operationClassName;

    private final Class<? extends Transform> transformClass;
    private final boolean isSpecDriven;

    /**
     * Process an element from the Chainr Spec into a ChainrEntry class.
     * This method tries to validate the syntax of the Chainr spec, whereas
     * the ChainrInstantiator deals with loading the Transform classes.
     *
     * @param chainrEntryObj the unknown Object from the Chainr list
     * @param index the index of the chainrEntryObj, used in reporting errors
     */
    public ChainrEntry( int index, Object chainrEntryObj ) {

        if ( ! (chainrEntryObj instanceof Map ) ) {
            throw new SpecException( "JOLT ChainrEntry expects a JSON map - Malformed spec at index:" + index );
        }

        @SuppressWarnings( "unchecked" ) // We know it is a Map due to the check above
        Map<String,Object> chainrEntryMap = (Map<String, Object>) chainrEntryObj;

        this.index = index;

        String opString = extractString( index, ChainrEntry.OPERATION_KEY, chainrEntryMap );

        if ( opString == null ) {
            throw new SpecException( "JOLT Chainr spec index:" + index + " should define an 'operation' that is a transform or transform factory." );
        }

        if ( STOCK_TRANSFORMS.containsKey( opString ) ) {
            operationClassName = STOCK_TRANSFORMS.get( opString );
        }
        else {
            operationClassName = opString;
        }

        transformClass = getTransformComponentClass();

        spec = chainrEntryMap.get( ChainrEntry.SPEC_KEY );
        isSpecDriven = SpecTransform.class.isAssignableFrom( transformClass );
        if ( isSpecDriven && ! chainrEntryMap.containsKey( SPEC_KEY ) ) {
            throw new SpecException( "JOLT Chainr - Transform className:" + transformClass.getCanonicalName() + " requires a spec." );
        }
    }

    private String extractString( int index, String key, Map<String, Object> chainrEntryMap ) {

        Object operationNameObj = chainrEntryMap.get( key );
        if ( operationNameObj == null ) {
            return null;
        }
        else if ( operationNameObj instanceof String) {
            if ( StringUtils.isBlank( (String) operationNameObj ) ) {
                throw new SpecException( "JOLT Chainr '" + ChainrEntry.OPERATION_KEY + "' should not be blank, spec index:" + index );
            }
            return (String) operationNameObj;
        }
        else {
            throw new SpecException( "JOLT Chainr needs a '" + ChainrEntry.OPERATION_KEY + "' of type String, spec index:" + index );
        }
    }

    private Class<? extends Transform> getTransformComponentClass() {

        try {
            Class opClass = Class.forName( getOperationClassName() );

            if ( Chainr.class.isAssignableFrom( opClass ) ) {
                throw new SpecException( "Attempt to nest Chainr inside itself at Chainr spec index:" + getIndex() );
            }

            if ( ! Transform.class.isAssignableFrom( opClass ) )
            {
                throw new SpecException( "JOLT Chainr class:" + operationClassName + " does not implement the Transform interface.  Chainr spec index:" + getIndex() );
            }

            @SuppressWarnings( "unchecked" ) // We know it is some type of Transform due to the check above
            Class<? extends Transform> transformClass = (Class<? extends Transform>) opClass;

            return transformClass;

        } catch ( ClassNotFoundException e ) {
            throw new SpecException( "JOLT Chainr could not find transform class :" + getOperationClassName() + ".  Chainr spec index:" + getIndex(), e );
        }
    }



    public int getIndex() {
        return index;
    }

    public Object getSpec() {
        return spec;
    }

    public String getOperationClassName() {
        return operationClassName;
    }

    public Class<? extends Transform> getTransformClass() {
        return transformClass;
    }

    public boolean isSpecDriven() {
        return isSpecDriven;
    }
}
