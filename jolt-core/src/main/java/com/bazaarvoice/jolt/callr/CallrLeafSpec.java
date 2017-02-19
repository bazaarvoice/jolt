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
package com.bazaarvoice.jolt.callr;

import com.bazaarvoice.jolt.common.pathelement.LiteralPathElement;
import com.bazaarvoice.jolt.common.pathelement.MatchablePathElement;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CallrLeafSpec extends CallrSpec {

    /**
     * Maps wrapper {@code Class}es to their corresponding primitive types.
     */
    private static final Map<Class<?>, Class<?>> wrapperPrimitiveMap = new HashMap<Class<?>, Class<?>>();

    static {
        wrapperPrimitiveMap.put( Boolean.class, Boolean.TYPE );
        wrapperPrimitiveMap.put( Byte.class, Byte.TYPE );
        wrapperPrimitiveMap.put( Character.class, Character.TYPE );
        wrapperPrimitiveMap.put( Short.class, Short.TYPE );
        wrapperPrimitiveMap.put( Integer.class, Integer.TYPE );
        wrapperPrimitiveMap.put( Long.class, Long.TYPE );
        wrapperPrimitiveMap.put( Double.class, Double.TYPE );
        wrapperPrimitiveMap.put( Float.class, Float.TYPE );
        wrapperPrimitiveMap.put( Void.TYPE, Void.TYPE );
    }

    protected final MatchablePathElement pathElement;
    protected final LiteralPathElement classElement;
    protected final LiteralPathElement methodElement;
    protected final List argsElement;

    public CallrLeafSpec( String rawJsonKey, Map<String, Object> rhs ) {
        super( rawJsonKey );
        PathElement pe = parse( rawJsonKey );
        if ( !( pe instanceof MatchablePathElement ) ) {
            throw new SpecException( "Spec LHS key=" + rawJsonKey + " is not a valid LHS key." );
        }
        this.pathElement = (MatchablePathElement) pe;

        if ( rhs.size() != 3 ) {
            throw new SpecException( "FunctionCaller has more parameters on its config that expected." );
        }

        pe = parse( (String) rhs.get( CLASS ) );
        if ( !( pe instanceof LiteralPathElement ) ) {
            throw new SpecException( "Spec LHS key=" + CLASS + " is not a valid LHS key." );
        }
        this.classElement = (LiteralPathElement) pe;

        pe = parse( (String) rhs.get( METHOD ) );
        if ( !( pe instanceof LiteralPathElement ) ) {
            throw new SpecException( "Spec LHS key=" + METHOD + " is not a valid LHS key." );
        }
        this.methodElement = (LiteralPathElement) pe;

        if ( !( rhs.get( ARGS ) instanceof List ) ) {
            throw new SpecException( "Spec LHS key=" + ARGS + " is not a valid LHS key." );
        }
        this.argsElement = (List) rhs.get( ARGS );
    }

    @Override
    public boolean apply( String inputKey, Object input, WalkedPath walkedPath, Object parentContainer ) {

        MatchedElement thisLevel = getMatch( inputKey, walkedPath );
        if ( thisLevel == null ) {
            return false;
        }
        Object result = applyFunction( input, this.classElement.getRawKey(), this.methodElement.getCanonicalForm(), this.argsElement );
        ( (Map<String, Object>) parentContainer ).put( inputKey, result );
        return true;
    }

    private Object applyFunction( Object input, String className, String method, List args ) {
        try {
            Object[] computedArgs = buildArgs( input, args ).toArray();
            return Class.forName( className ).getMethod( method, getClassArgs( computedArgs ) )
                    .invoke( input, computedArgs );
        } catch ( NoSuchMethodException nsme ) {
            throw new TransformException( "The Spec provided cannot find method " + methodElement.getCanonicalForm(), nsme );
        } catch ( InvocationTargetException | IllegalAccessException ie ) {
            throw new TransformException( "Exception invoking method " + methodElement.getCanonicalForm(), ie );
        } catch ( ClassNotFoundException cnfe ) {
            throw new TransformException( "Class " + classElement.getCanonicalForm() + " not found", cnfe );
        }
    }

    private List buildArgs( Object input, List args ) {
        List builtArgs = new ArrayList();
        for ( Object arg : args ) {
            if ( arg instanceof Map && isLeaf( (Map<String, Object>) arg ) ) {
                Object computedArg = applyFunction( input,
                        (String) ( (Map<String, Object>) arg ).get( CLASS ),
                        (String) ( (Map<String, Object>) arg ).get( METHOD ),
                        (List) ( (Map<String, Object>) arg ).get( ARGS ) );
                builtArgs.add( computedArg );
            } else {
                builtArgs.add( arg );
            }
        }
        return builtArgs;
    }

    private Class[] getClassArgs( Object[] args ) {
        if ( args == null || args.length == 0 ) {
            return new Class[]{};
        }
        Class[] argsClass = new Class[ args.length ];
        for ( int i = 0; i < args.length; i++ ) {
            Class c = wrapperPrimitiveMap.get( args[ i ].getClass() );
            argsClass[ i ] = c == null ? args[ i ].getClass() : c;
        }
        return argsClass;
    }

    private MatchedElement getMatch( String inputKey, WalkedPath walkedPath ) {
        return pathElement.match( inputKey, walkedPath );
    }
}
