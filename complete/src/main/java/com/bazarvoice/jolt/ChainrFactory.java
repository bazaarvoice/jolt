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
package com.bazarvoice.jolt;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.bazaarvoice.jolt.chainr.instantiator.ChainrInstantiator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * A factory class with various static methods that return instances of Chainr.
 */
public class ChainrFactory {

    /**
     * Builds a Chainr instance using the spec described in the data via the class path that is passed in.
     *
     * @param chainrSpecClassPath The class path that points to the chainr spec.
     * @return a Chainr instance
     */
    public static Chainr fromClassPath( String chainrSpecClassPath ) {
        return fromClassPath( chainrSpecClassPath, null );
    }

    /**
     * Builds a Chainr instance using the spec described in the data via the class path that is passed in.
     *
     * @param chainrSpecClassPath The class path that points to the chainr spec.
     * @param chainrInstantiator the ChainrInstantiator to use to initialze the Chainr instance
     * @return a Chainr instance
     */
    public static Chainr fromClassPath( String chainrSpecClassPath, ChainrInstantiator chainrInstantiator ) {
        InputStream inputStream = Object.class.getResourceAsStream( chainrSpecClassPath );
        return getChainr( chainrInstantiator, inputStream, chainrSpecClassPath );
    }

    /**
     * Builds a Chainr instance using the spec described in the data via the file path that is passed in.
     *
     * @param chainrSpecFilePath The file path that points to the chainr spec.
     * @return a Chainr instance
     */
    public static Chainr fromFileSystem( String chainrSpecFilePath ) {
        return fromFileSystem( chainrSpecFilePath, null );
    }

    /**
     * Builds a Chainr instance using the spec described in the data via the file path that is passed in.
     *
     * @param chainrSpecFilePath The file path that points to the chainr spec.
     * @param chainrInstantiator the ChainrInstantiator to use to initialze the Chainr instance
     * @return a Chainr instance
     */
    public static Chainr fromFileSystem( String chainrSpecFilePath, ChainrInstantiator chainrInstantiator ) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream( chainrSpecFilePath );
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( "Unable to load chainr spec file " + chainrSpecFilePath );
        }
        return getChainr( chainrInstantiator, fileInputStream, chainrSpecFilePath );
    }

    /**
     * Builds a Chainr instance using the spec described in the File that is passed in.
     *
     * @param chainrSpecFile The File which contains the chainr spec.
     * @return a Chainr instance
     */
    public static Chainr fromFile( File chainrSpecFile ) {
        return fromFile( chainrSpecFile, null );
    }

    /**
     * Builds a Chainr instance using the spec described in the File that is passed in.
     *
     * @param chainrSpecFile The File which contains the chainr spec.
     * @param chainrInstantiator the ChainrInstantiator to use to initialze the Chainr instance
     * @return a Chainr instance
     */
    public static Chainr fromFile( File chainrSpecFile, ChainrInstantiator chainrInstantiator ) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream( chainrSpecFile );
        } catch ( FileNotFoundException e ) {
            throw new RuntimeException( "Unable to load chainr spec file " + chainrSpecFile.getAbsolutePath() );
        }
        return getChainr( chainrInstantiator, fileInputStream, chainrSpecFile.getAbsolutePath() );
    }

    /**
     * The main engine in ChainrFactory for building a Chainr Instance.
     *
     * @param chainrInstantiator The ChainrInstantiator to use. If null it will not be used.
     * @param inputStream The input stream to read the json data from
     * @param filePath The path to the file
     * @return the Chainr instance created from the chainrInstantiator and inputStream
     */
    private static Chainr getChainr( ChainrInstantiator chainrInstantiator, InputStream inputStream, String filePath ) {
        Chainr chainr;
        try {
            Object chainrSpec = JsonUtils.jsonToObject( inputStream );
            if (chainrInstantiator == null ) {
                chainr = Chainr.fromSpec( chainrSpec );
            }
            else {
                chainr = Chainr.fromSpec( chainrSpec, chainrInstantiator );
            }
        } catch ( IOException e ) {
            throw new RuntimeException( "Unable to load chainr spec file " + filePath );
        }
        return chainr;
    }
}
