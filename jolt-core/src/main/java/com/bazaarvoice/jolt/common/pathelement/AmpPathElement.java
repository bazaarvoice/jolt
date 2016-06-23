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
package com.bazaarvoice.jolt.common.pathelement;

import com.bazaarvoice.jolt.common.reference.AmpReference;
import com.bazaarvoice.jolt.common.tree.MatchedElement;
import com.bazaarvoice.jolt.common.tree.WalkedPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * PathElement class that handles keys with & values, like input: "photos-&(1,1)""
 * It breaks down the string into a series of String or Reference tokens, that can be used to
 * 1) match input like "photos-5" where "&(1,1)" evaluated to 5
 */
public class AmpPathElement extends BasePathElement implements MatchablePathElement, EvaluatablePathElement {

    private final List<Object> tokens;
    private final String canonicalForm;

    public AmpPathElement( String key ) {
        super(key);

        StringBuilder literal = new StringBuilder();
        StringBuilder canonicalBuilder = new StringBuilder();

        ArrayList<Object> tok = new ArrayList<>();
        int index = 0;
        while( index < key.length() ) {

            char c = key.charAt( index );

            // beginning of reference
            if ( c == '&' ) {

                // store off any literal text captured thus far
                if ( literal.length() > 0 ) {
                    tok.add( literal.toString() );
                    canonicalBuilder.append( literal );
                    literal = new StringBuilder();
                }

                int refEnd = findEndOfReference( key.substring( index + 1 ) );
                AmpReference ref = new AmpReference(key.substring(index, index + refEnd + 1) );
                canonicalBuilder.append( ref.getCanonicalForm() );

                tok.add( ref );
                index += refEnd;
            }
            else {
                literal.append( c );
            }
            index++;
        }
        if ( literal.length() > 0 ) {
            tok.add( literal.toString() );
            canonicalBuilder.append( literal.toString() );
        }

        tok.trimToSize();

        tokens = Collections.unmodifiableList( tok );
        canonicalForm = canonicalBuilder.toString();
    }

    private static int findEndOfReference( String key ) {
        if( "".equals( key ) ) {
            return 0;
        }

        for( int index = 0; index < key.length(); index++ ){
            char c = key.charAt( index );
            // keep going till we see something other than a digit, parens, or comma
            if( ! Character.isDigit( c ) && c != '(' && c != ')' && c != ',') {
                return index;
            }
        }
        return key.length();
    }

    @Override
    public String getCanonicalForm() {
        return canonicalForm;
    }

    // Visible for testing
    public List<Object> getTokens() {
        return tokens;
    }

    @Override
    public String evaluate( WalkedPath walkedPath ) {

        // Walk thru our tokens and build up a string
        // Use the supplied Path to fill in our token References
        StringBuilder output = new StringBuilder();

        for ( Object token : tokens ) {
            if ( token instanceof String ) {
                output.append( token );
            }
            else {
                AmpReference ref = (AmpReference) token;
                MatchedElement matchedElement = walkedPath.elementFromEnd( ref.getPathIndex() ).getMatchedElement();
                String value = matchedElement.getSubKeyRef( ref.getKeyGroup() );
                output.append( value );
            }
        }

        return output.toString();
    }

    @Override
    public MatchedElement match( String dataKey, WalkedPath walkedPath ) {
        String evaled = evaluate( walkedPath );
        if ( evaled.equals( dataKey ) ) {
            return new MatchedElement( evaled );
        }
        return null;
    }
}
