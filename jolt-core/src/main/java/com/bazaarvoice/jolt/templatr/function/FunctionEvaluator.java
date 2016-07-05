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

package com.bazaarvoice.jolt.templatr.function;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.common.tree.WalkedPath;

import java.util.Map;

public class FunctionEvaluator {

    public static FunctionEvaluator of(Function function, FunctionArg[] functionArgs) {
        return new FunctionEvaluator( function, functionArgs );
    }

    // function that is evaluated and applied as output
    private final Function function;
    // arguments of the function, not evaluated and can be a jolt path expression that
    // either point to a context or self, or a value present at the matching level
    private final FunctionArg[] functionArgs;

    private FunctionEvaluator( final Function function, final FunctionArg[] functionArgs ) {
        this.function = function;
        this.functionArgs = functionArgs;
    }


    public Optional<Object> evaluate(Object input, WalkedPath walkedPath, Map<String, Object> context) {
        Optional<Object> valueOptional = Optional.empty();

        try {
            Object[] evaluatedArgs;

            // "key": "@0", "key": literal
            if(function == null) {
                valueOptional = functionArgs[0].evaluateArg( walkedPath, context );
            }
            // "key": "=abs(@(1,&0))"
            else if( functionArgs != null ) {
                evaluatedArgs = evaluateArgsValue( functionArgs, context, walkedPath );
                valueOptional = function.apply( evaluatedArgs );
            }
            // "key": "=abs"
            else {
                evaluatedArgs = new Object[] {input}; // pass current value as arg
                valueOptional = function.apply( evaluatedArgs );
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
            evaluatedArgs[i] = evaluatedValue.isPresent() ? evaluatedValue.get() : null;
        }
        return evaluatedArgs;
    }
}
