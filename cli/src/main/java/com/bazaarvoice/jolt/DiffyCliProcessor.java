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
 * The JoltCliProcessor for Diffy. See https://github.com/bazaarvoice/jolt/blob/master/json-utils/src/main/java/com/bazaarvoice/jolt/Diffy.java
 * for documentation on  Diffy.
 */
public class DiffyCliProcessor implements JoltCliProcessor {

    /**
     * Initialize the arg parser for the Diffy sub command
     *
     * @param subparsers The Subparsers object to attach the new Subparser to
     */
    @Override
    public void intializeSubCommand( Subparsers subparsers ) {
        Subparser diffyParser = subparsers.addParser( "diffy" )
                .description( "Jolt CLI Diffy Tool. This tool will ingest two JSON inputs (from files or standard input) and " +
                        "perform the Jolt Diffy operation to detect any differences. The program will return an exit code of " +
                        "0 if no differences are found or a 1 if a difference is found or an error is encountered." )
                .defaultHelp( true );

        diffyParser.addArgument( "filePath1" ).help( "File path to feed to Input #1 for the Diffy operation. " +
                "This file should contain valid JSON." )
                .type( Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead() );
        diffyParser.addArgument( "filePath2" ).help( "File path to feed to Input #2 for the Diffy operation. " +
                "This file should contain valid JSON. " +
                "If this argument is not specified then standard input will be used." )
                .type( Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead() )
                .nargs( "?" ).setDefault( (File) null );   // these last two method calls make filePath2 optional

        diffyParser.addArgument( "-s" ).help( "Diffy will suppress output and run silently." )
                .action( Arguments.storeTrue() );
        diffyParser.addArgument( "-a" ).help( "Diffy will not consider array order when detecting differences" )
                .action( Arguments.storeTrue() );
    }

    /**
     * Process the Diffy Subcommand
     *
     * @param ns Namespace which contains parsed commandline arguments
     * @return true if no differences are found, false if a difference is found or an error occurs
     */
    @Override
    public boolean process( Namespace ns ) {
        boolean suppressOutput = ns.getBoolean( "s" );

        Object jsonObject1 = JoltCliUtilities.createJsonObjectFromFile( (File) ns.get( "filePath1" ), suppressOutput );
        File file = ns.get( "filePath2" );
        Object jsonObject2 = JoltCliUtilities.readJsonInput( file, suppressOutput );

        Diffy diffy;
        if ( ns.getBoolean( "a" ) ) {
            diffy = new ArrayOrderObliviousDiffy();
        } else {
            diffy = new Diffy();
        }
        Diffy.Result result = diffy.diff( jsonObject1, jsonObject2 );

        if ( result.isEmpty() ) {
            JoltCliUtilities.printToStandardOut( "Diffy found no differences", suppressOutput );
            return true;
        } else {
            try {
                JoltCliUtilities.printToStandardOut( "Differences found. Input #1 contained this:\n" +
                        JsonUtils.toPrettyJsonString( result.expected ) + "\n" +
                        "Input #2 contained this:\n" +
                        JsonUtils.toPrettyJsonString( result.actual ), suppressOutput );

            }
            catch ( Exception e ) {
                JoltCliUtilities.printToStandardOut( "Differences found, but diffy encountered an error while writing the result.", suppressOutput );
            }
            return false;
        }
    }
}
