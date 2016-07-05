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

package com.bazaarvoice.jolt.templatr.spec;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.SpecStringParser;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.templatr.OpMode;
import com.bazaarvoice.jolt.templatr.TemplatrSpecBuilder;
import com.bazaarvoice.jolt.templatr.function.Function;
import com.bazaarvoice.jolt.templatr.function.FunctionArg;
import com.bazaarvoice.jolt.templatr.function.FunctionEvaluator;

import java.util.List;
import java.util.Map;

public class TemplatrLeafSpec extends TemplatrSpec {

    private final FunctionEvaluator functionEvaluator;

    @SuppressWarnings( "unchecked" )
    public TemplatrLeafSpec( final String rawJsonKey, Object rhsObj, final OpMode opMode, final Map<String, Function> functionsMap ) {
        super(rawJsonKey, opMode);

        final Function function;
        final FunctionArg[] functionArgs;

        // "key": anyObjectOrLiteral --- just set as-is
        if ( !(rhsObj instanceof String) ) {
            function = null;
            functionArgs = new FunctionArg[]{ FunctionArg.forLiteral( rhsObj ) };
        }
        else {
            String rhs = (String) rhsObj;
            // "key": "@0" --- evaluate expression then set
            if(!rhs.startsWith( TemplatrSpecBuilder.FUNCTION )) {
                function = null;
                functionArgs = new FunctionArg[]{constructSingleArg( rhs )};
            }
            else {
                String functionName;
                // "key": "=abs" --- call function with current value then set output if present
                if ( !rhs.contains( "(" ) && !rhs.endsWith( ")" ) ) {
                    functionName = rhs.substring( TemplatrSpecBuilder.FUNCTION.length() );
                    functionArgs = null;
                }
                // "key": "=abs(@(1,&0))" --- evaluate expression then call function with
                //                            expression-output, then set output if present
                else {
                    String fnString = rhs.substring( TemplatrSpecBuilder.FUNCTION.length() );
                    List<String> fnArgs = SpecStringParser.parseFunctionArgs( fnString );
                    functionName = fnArgs.remove( 0 );
                    functionArgs = constructArgs( fnArgs );
                }
                // sanity check, should happen only if provided function name is different than what is provided
                if ( (function = functionsMap.get( functionName )) == null ) {
                    throw new SpecException( "Invalid " + opMode.name() + " spec LHS. No function named:" + functionName + " available. Spec in question " + rawJsonKey + " : " + rhs );
                }
            }
        }
        // sanity check --- this should not happen though!
        if( function == null && ( functionArgs.length == 0 ) ) {
            throw new SpecException( "Invalid " + opMode.name() + " spec LHS. Spec in question " + rawJsonKey + " : " + rhsObj );
        }
        functionEvaluator = FunctionEvaluator.of( function, functionArgs );
    }

    @Override
    public void applyElement( final String inputKey, final Object input, final MatchedElement thisLevel, final WalkedPath walkedPath, final Map<String, Object> context ) {

        Object parent = walkedPath.lastElement().getTreeRef();
        Optional<Object> valueOptional = Optional.empty();
        walkedPath.add( input, thisLevel );

        try {
            valueOptional = functionEvaluator.evaluate( input, walkedPath, context );
        }
        catch(Exception ignored) {}

        if(valueOptional.isPresent()) {
            setData( parent, thisLevel, valueOptional.get(), opMode );
        }
        walkedPath.removeLast();
    }

    private static FunctionArg[] constructArgs( List<String> argsList ) {
        FunctionArg[] argsArray = new FunctionArg[argsList.size()];
        for(int i=0; i<argsList.size(); i++) {
            String arg = argsList.get( i );
            argsArray[i] = constructSingleArg( arg );
        }
        return argsArray;
    }

    private static FunctionArg constructSingleArg( String arg ) {
        if(arg.startsWith( TemplatrSpecBuilder.CARET )) {
            return FunctionArg.forContext( TRAVERSAL_BUILDER.build( arg.substring( 1 ) ) );
        }
        else if(arg.startsWith( TemplatrSpecBuilder.AT )) {
            return FunctionArg.forSelf( TRAVERSAL_BUILDER.build( arg ) );
        }
        else {
            return FunctionArg.forLiteral( arg );
        }
    }
}
