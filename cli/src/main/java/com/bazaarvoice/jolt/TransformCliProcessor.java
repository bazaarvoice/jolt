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

import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

import java.io.File;

/**
 * The JoltCliProcessor for Chainr. See https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Chainr.java
 * for documentation on  Chainr.
 */
public class TransformCliProcessor implements JoltCliProcessor {

    private static final boolean SUPPRESS_OUTPUT = false;

    /**
     * Initialize the arg parser for the Transform sub command
     *
     * @param subparsers The Subparsers object to attach the new Subparser to
     */
    @Override
    public void intializeSubCommand( Subparsers subparsers ) {
        Subparser transformParser = subparsers.addParser( "transform" )
                .description( "Jolt CLI Transform Tool. This tool will ingest a JSON spec file and an JSON input (from a file or " +
                        "standard input) and run the transforms specified in the spec file on the input. The program will return an " +
                        "exit code of 0 if the input is transformed successfully or a 1 if an error is encountered" )
                .defaultHelp( true );

        File nullFile = null;
        transformParser.addArgument( "spec" ).help( "File path to Jolt Transform Spec to execute on the input. " +
                "This file should contain valid JSON." )
                .type( Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead() );
        transformParser.addArgument( "input" ).help( "File path to the input JSON for the Jolt Transform operation. " +
                "This file should contain valid JSON. " +
                "If this argument is not specified then standard input will be used." )
                .type( Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead() )
                .nargs( "?" ).setDefault( nullFile );   // these last two method calls make input optional

        transformParser.addArgument( "-u" ).help( "Turns off pretty print for the output. Output will be raw json with no formatting." )
                .action( Arguments.storeTrue() );
    }

    /**
     * Process the transform sub command
     *
     * @param ns Namespace which contains parsed commandline arguments
     * @return true if the transform is successful, false if an error occured
     */
    @Override
    public boolean process( Namespace ns ) {

        Chainr chainr;
        try {
            chainr = ChainrFactory.fromFile((File) ns.get("spec"));
        } catch ( Exception e ) {
            JoltCliUtilities.printToStandardOut( "Chainr failed to load spec file.", SUPPRESS_OUTPUT );
            e.printStackTrace( System.out );
            return false;
        }

        File file = ns.get( "input" );
        Object input = JoltCliUtilities.readJsonInput( file, SUPPRESS_OUTPUT );

        Object output;
        try {
            output = chainr.transform( input );
        } catch ( Exception e ) {
            JoltCliUtilities.printToStandardOut( "Chainr failed to run spec file.", SUPPRESS_OUTPUT );
            return false;
        }

        Boolean uglyPrint = ns.getBoolean( "u" );
        return JoltCliUtilities.printJsonObject( output, uglyPrint, SUPPRESS_OUTPUT );
    }

}
