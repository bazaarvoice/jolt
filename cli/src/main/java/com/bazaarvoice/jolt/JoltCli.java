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
package com.bazaarvoice.jolt;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class JoltCli {

    private static final Map<String, JoltCliProcessor> JOLT_CLI_PROCESSOR_MAP;

    static {
        Map<String, JoltCliProcessor> temp = new HashMap<>();
        temp.put( JoltCliUtilities.DIFFY_COMMAND_IDENTIFIER, new DiffyCliProcessor() );
        temp.put( JoltCliUtilities.SORT_COMMAND_IDENTIFIER, new SortCliProcessor() );
        temp.put( JoltCliUtilities.TRANSFORM_COMMAND_IDENTIFIER, new TransformCliProcessor() );

        JOLT_CLI_PROCESSOR_MAP = Collections.unmodifiableMap( temp );
    }

    public static void main( String[] args ) {
        System.exit( runJolt( args ) ? 0 : 1 );
    }

    /**
     * The logic for running DiffyTool has been captured in a helper method that returns a boolean to facilitate unit testing.
     * Since System.exit terminates the JVM it would not be practical to test the main method.
     *
     * @param args the arguments from the command line input
     * @return true if two inputs were read with no differences, false if differences were found or an error was encountered
     */
    protected static boolean runJolt( String[] args ) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser( "jolt" );
        Subparsers subparsers = parser.addSubparsers().help( "transform: given a Jolt transform spec, runs the specified transforms on the input data.\n" +
                "diffy: diff two JSON documents.\n" +
                "sort: sort a JSON document alphabetically for human readability." );

        for ( Map.Entry<String, JoltCliProcessor> entry : JOLT_CLI_PROCESSOR_MAP.entrySet() ) {
            entry.getValue().intializeSubCommand( subparsers );
        }

        Namespace ns;
        try {
            ns = parser.parseArgs( args );
        } catch ( ArgumentParserException e ) {
            parser.handleError( e );
            return false;
        }

        JoltCliProcessor joltToolProcessor = JOLT_CLI_PROCESSOR_MAP.get( args[0] );
        if ( joltToolProcessor != null ) {
            return joltToolProcessor.process( ns );
        } else {
            // TODO: error message, print usage. although I don't think it will ever get to this point.
            return false;
        }
    }

}
