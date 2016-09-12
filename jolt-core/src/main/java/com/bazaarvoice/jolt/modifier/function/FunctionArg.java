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
import com.bazaarvoice.jolt.common.PathEvaluatingTraversal;
import com.bazaarvoice.jolt.common.pathelement.PathElement;
import com.bazaarvoice.jolt.common.pathelement.TransposePathElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;
import com.bazaarvoice.jolt.exception.SpecException;

import java.util.Map;

public abstract class FunctionArg {

    public static FunctionArg forSelf(PathEvaluatingTraversal traversal) {
        return new SelfLookupArg( traversal );
    }

    private static final class SelfLookupArg extends FunctionArg {
        private final TransposePathElement pathElement;

        private SelfLookupArg( PathEvaluatingTraversal traversal ) {
            PathElement pathElement = traversal.get( traversal.size() - 1 );
            if(pathElement instanceof TransposePathElement ) {
                this.pathElement = (TransposePathElement) pathElement;
            }
            else {
                throw new SpecException( "Expected @ path element here" );
            }
        }

        @Override
        public Optional<Object> evaluateArg( final WalkedPath walkedPath, final Map<String, Object> context ) {
            return pathElement.objectEvaluate( walkedPath );
        }
    }

    public static FunctionArg forContext(PathEvaluatingTraversal traversal) {
        return new ContextLookupArg( traversal );
    }

    private static final class ContextLookupArg extends FunctionArg {
        private final PathEvaluatingTraversal traversal;

        private ContextLookupArg( PathEvaluatingTraversal traversal ) {
            this.traversal = traversal;
        }

        @Override
        public Optional<Object> evaluateArg( final WalkedPath walkedPath, final Map<String, Object> context ) {
            return traversal.read( context, walkedPath );
        }
    }

    public static FunctionArg forLiteral( Object obj, boolean parseArg ) {
        if(parseArg) {
            if ( obj instanceof String ) {
                String arg = (String) obj;
                if ( arg.length() == 0 ) {
                    return new LiteralArg( null );
                }
                else if ( arg.startsWith( "'" ) && arg.endsWith( "'" ) ) {
                    return new LiteralArg( arg.substring( 1, arg.length() - 1 ) );
                }
                else if ( arg.equalsIgnoreCase( "true" ) || arg.equalsIgnoreCase( "false" ) ) {
                    return new LiteralArg( Boolean.parseBoolean( arg ) );
                }
                else {
                    Optional<?> optional = Objects.toNumber( arg );
                    if(optional.isPresent()) {
                        return new LiteralArg( optional.get() );
                    }
                    return new LiteralArg( arg );
                }
            }
            else {
                return new LiteralArg( obj );
            }
        }
        else {
            return new LiteralArg( obj );
        }
    }

    private static final class LiteralArg extends FunctionArg {

        private final Optional<Object> returnValue;

        private LiteralArg( final Object object ) {
            this.returnValue = Optional.of( object );
        }

        @Override
        public Optional<Object> evaluateArg( final WalkedPath walkedPath, final Map<String, Object> context ) {
            return returnValue;
        }
    }

    public abstract Optional<Object> evaluateArg(WalkedPath walkedPath, Map<String, Object> context);
}
