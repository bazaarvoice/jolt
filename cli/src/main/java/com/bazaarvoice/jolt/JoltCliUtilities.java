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

import com.fasterxml.jackson.core.JsonParseException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A utility class for the Jolt CLI tool.
 */
public class JoltCliUtilities {

    public static final String DIFFY_COMMAND_IDENTIFIER = "diffy";
    public static final String SORT_COMMAND_IDENTIFIER = "sort";
    public static final String TRANSFORM_COMMAND_IDENTIFIER = "transform";

    /**
     * Prints the given string to standard out, or doesn't, based on the suppressOutput flag
     */
    public static void printToStandardOut( String output, boolean suppressOutput ) {
        if ( !suppressOutput ) {
            System.out.println( output );
        }
    }

    /**
     * Uses the File to build a Map containing JSON data found in the file. This method will
     * System exit with an error code of 1 if has any trouble opening the file or the file did not
     * contain properly formatted JSON (i.e. the JSON parser was unable to parse its contents)
     *
     * @return the Map containing the JSON data
     */
    public static Object createJsonObjectFromFile( File file, boolean suppressOutput ) {
        Object jsonObject = null;
        try {
            FileInputStream inputStream = new FileInputStream( file );
            jsonObject = JsonUtils.jsonToObject( inputStream );
            inputStream.close();
        } catch ( IOException e ) {
            if ( e instanceof JsonParseException ) {
                printToStandardOut( "File " + file.getAbsolutePath() + " did not contain properly formatted JSON.", suppressOutput );
            } else {
                printToStandardOut( "Failed to open file: " + file.getAbsolutePath(), suppressOutput );
            }
            System.exit( 1 );
        }
        return jsonObject;
    }

    /**
     * Prints the given json object to standard out, accounting for pretty printing and suppressed output.
     *
     * @param output The object to print. This method will fail if this object is not well formed JSON.
     * @param uglyPrint ignore pretty print
     * @param suppressOutput suppress output to standard out
     * @return true if printing operation was successful
     */
    public static boolean printJsonObject( Object output, Boolean uglyPrint, boolean suppressOutput ) {
        try {
            if ( uglyPrint ) {
                printToStandardOut( JsonUtils.toJsonString( output ), suppressOutput );
            } else {
                printToStandardOut( JsonUtils.toPrettyJsonString( output ), suppressOutput );
            }
        } catch ( Exception e ) {
            printToStandardOut( "An error occured while attempting to print the output.", suppressOutput );
            return false;
        }
        return true;
    }

    /**
     * This method will read in JSON, either from the given file or from standard in
     * if the file is null. An object contain the ingested input is returned.
     *
     * @param file the file to read the input from, or null to use standard in
     * @param suppressOutput suppress output of error messages to standard out
     * @return Object containing input if successful or null if an error occured
     */
    public static Object readJsonInput( File file, boolean suppressOutput ) {
        Object jsonObject;
        if ( file == null ) {
            try {
                jsonObject = JsonUtils.jsonToMap( System.in );
            } catch ( Exception e ) {
                printToStandardOut( "Failed to process standard input.", suppressOutput );
                return null;
            }
        } else {
            jsonObject = createJsonObjectFromFile( file, suppressOutput );
        }
        return jsonObject;
    }
}
