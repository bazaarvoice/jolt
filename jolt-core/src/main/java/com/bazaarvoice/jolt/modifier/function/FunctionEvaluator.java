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

package com.bazaarvoice.jolt.modifier.function;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.tree.WalkedPath;

import java.util.Map;

@SuppressWarnings( "deprecated" )
public class FunctionEvaluator {

    public static FunctionEvaluator forFunctionEvaluation( Function function, FunctionArg... functionArgs ) {
        return new FunctionEvaluator( function, functionArgs );
    }

    public static FunctionEvaluator forArgEvaluation( FunctionArg functionArgs ) {
        return new FunctionEvaluator( null, functionArgs );
    }

    // function that is evaluated and applied as output
    private final Function function;
    // arguments of the function, not evaluated and can be a jolt path expression that
    // either point to a context or self, or a value present at the matching level
    private final FunctionArg[] functionArgs;

    private FunctionEvaluator( final Function function, final FunctionArg... functionArgs ) {
        this.function = function;
        this.functionArgs = functionArgs;
    }


    public Optional<Object> evaluate(Optional<Object> inputOptional, WalkedPath walkedPath, Map<String, Object> context) {

        Optional<Object> valueOptional = Optional.empty();
        try {

            // "key": "@0", "key": literal
            if(function == null) {
                valueOptional = functionArgs[0].evaluateArg( walkedPath, context );
            }
            // "key": "=abs(@(1,&0))"
            // this is most usual case, a single argument is passed and we need to evaluate and
            // pass the value, if present, to the spec function
            else if( functionArgs.length == 1 ) {
                Optional<Object> evaluatedArgValue = functionArgs[0].evaluateArg( walkedPath, context );
                valueOptional = evaluatedArgValue.isPresent() ? function.apply( evaluatedArgValue.get() ): function.apply( );
            }
            // "key": "=abs(@(1,&0),-1,-3)"
            // this is more complicated case! if args is an array, after evaluation we cannot pass a missing value wrapped in
            // object[] into function. In such case null will be passed however, in json null is also a valid value, so it is
            // upto the implementer to interpret the value. Ideally we can almost always pass a list straight from input.
            else if( functionArgs.length > 1 ) {
                Object[] evaluatedArgs = evaluateArgsValue( functionArgs, context, walkedPath );
                valueOptional = function.apply( evaluatedArgs );
            }
            //
            // FYI this is where the "magic" happens that allows functions that take a single method
            //  default to the current "match" rather than an explicit "reference".
            // Note, this does not work for functions that take more than a single input.
            //
            // "key": "=abs"
            else {
                // pass current value as arg if present
                valueOptional = inputOptional.isPresent() ? function.apply( inputOptional.get()) : function.apply(  );
            }
        }
        catch(Exception ignored) {}

        return valueOptional;

    }

    private static Object[] evaluateArgsValue( final FunctionArg[] functionArgs, final Map<String, Object> context, final WalkedPath walkedPath ) {

        Object[] evaluatedArgs = new Object[functionArgs.length];
        for(int i=0; i<functionArgs.length; i++) {
            FunctionArg arg = functionArgs[i];
            Optional<Object> evaluatedValue = arg.evaluateArg( walkedPath, context );
            evaluatedArgs[i] = evaluatedValue.get();
        }
        return evaluatedArgs;
    }
}
