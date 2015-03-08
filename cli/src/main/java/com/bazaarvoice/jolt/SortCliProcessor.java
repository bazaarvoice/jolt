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
 * The JoltCliProcessor for Sortr. See https://github.com/bazaarvoice/jolt/blob/master/jolt-core/src/main/java/com/bazaarvoice/jolt/Sortr.java
 * for documentation on  Sortr.
 */
public class SortCliProcessor implements JoltCliProcessor {

    private static final boolean SUPPRESS_OUTPUT = false;

    /**
     * Initialize the arg parser for the Sort sub command
     *
     * @param subparsers The Subparsers object to attach the new Subparser to
     */
    @Override
    public void intializeSubCommand( Subparsers subparsers ) {
        Subparser sortParser = subparsers.addParser( "sort" )
                .description( "Jolt CLI Sort Tool. This tool will ingest one JSON input (from a file or standard input) and " +
                        "perform the Jolt sort operation on it. The sort order is standard alphabetical ascending, with a " +
                        "special case for \"~\" prefixed keys to be bumped to the top. The program will return an exit code " +
                        "of 0 if the sort operation is performed successfully or a 1 if an error is encountered." )
                .defaultHelp( true );

        sortParser.addArgument( "input" ).help( "File path to the input JSON that the sort operation should be performed on. " +
                "This file should contain valid JSON. " +
                "If this argument is not specified then standard input will be used." )
                .type( Arguments.fileType().verifyExists().verifyIsFile().verifyCanRead() )
                .nargs( "?" ).setDefault( (File) null ).required( false );   // these last two method calls make input optional

        sortParser.addArgument( "-u" ).help( "Turns off pretty print for the output. Output will be raw json with no formatting." )
                .action( Arguments.storeTrue() );
    }

    /**
     *
     * @param ns Namespace which contains parsed commandline arguments
     * @return true if the sort was successful, false if an error occurred
     */
    @Override
    public boolean process( Namespace ns ) {

        File file = ns.get( "input" );
        Object jsonObject = JoltCliUtilities.readJsonInput( file, SUPPRESS_OUTPUT );
        if ( jsonObject == null ) {
            return false;
        }

        Sortr sortr = new Sortr();
        Object output = sortr.transform( jsonObject );
        Boolean uglyPrint = ns.getBoolean( "u" );
        return JoltCliUtilities.printJsonObject( output, uglyPrint, SUPPRESS_OUTPUT );
    }

}
