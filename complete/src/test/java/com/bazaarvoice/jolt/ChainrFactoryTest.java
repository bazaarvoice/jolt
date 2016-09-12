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

import com.bazaarvoice.jolt.chainr.instantiator.DefaultChainrInstantiator;
import com.bazaarvoice.jolt.exception.JsonUnmarshalException;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

public class ChainrFactoryTest {

    private static final String MALFORMED_INPUT_FILENAME = "malformed-input.json";
    private static final String WELLFORMED_INPUT_FILENAME = "wellformed-input.json";

    private static final String CLASSPATH = "/json/";
    private String fileSystemPath;
    private File wellformedFile;
    private File malformedFile;

    @BeforeClass
    public void setup() {
        fileSystemPath = getFileSystemPath();
        wellformedFile = new File( fileSystemPath + WELLFORMED_INPUT_FILENAME );
        malformedFile = new File( fileSystemPath + MALFORMED_INPUT_FILENAME );
    }

    @Test
    public void testGetChainrInstanceFromClassPath_success()
            throws Exception {
        Chainr result = ChainrFactory.fromClassPath( CLASSPATH + WELLFORMED_INPUT_FILENAME );
        Assert.assertNotNull( result, "ChainrFactory did not return an instance of Chainr." );
    }

    @Test( expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Unable to load JSON.*" )
    public void testGetChainrInstanceFromClassPath_error()
            throws Exception {
        ChainrFactory.fromClassPath( CLASSPATH + MALFORMED_INPUT_FILENAME );
    }

    @Test
    public void testGetChainrInstanceFromClassPathWithInstantiator_success()
            throws Exception {
        Chainr result = ChainrFactory.fromClassPath( CLASSPATH + WELLFORMED_INPUT_FILENAME, new DefaultChainrInstantiator() );
        Assert.assertNotNull( result, "ChainrFactory did not return an instance of Chainr." );
    }

    @Test( expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Unable to load JSON.*" )
    public void testGetChainrInstanceFromClassPathWithInstantiator_error()
            throws Exception {
        ChainrFactory.fromClassPath( CLASSPATH + MALFORMED_INPUT_FILENAME, new DefaultChainrInstantiator() );
    }

    @Test
    public void testGetChainrInstanceFromFileSystem_success()
            throws Exception {
        Chainr result = ChainrFactory.fromFileSystem( fileSystemPath + WELLFORMED_INPUT_FILENAME );
        Assert.assertNotNull( result, "ChainrFactory did not return an instance of Chainr." );
    }

    @Test( expectedExceptions = JsonUnmarshalException.class, expectedExceptionsMessageRegExp = "Unable to unmarshal JSON.*" )
    public void testGetChainrInstanceFromFileSystem_error()
            throws Exception {
        ChainrFactory.fromFileSystem( fileSystemPath + MALFORMED_INPUT_FILENAME );
    }

    @Test
    public void testGetChainrInstanceFromFileSystemWithInstantiator_success()
            throws Exception {
        Chainr result = ChainrFactory.fromFileSystem( fileSystemPath + WELLFORMED_INPUT_FILENAME, new DefaultChainrInstantiator() );
        Assert.assertNotNull( result, "ChainrFactory did not return an instance of Chainr." );
    }

    @Test(expectedExceptions = JsonUnmarshalException.class, expectedExceptionsMessageRegExp = "Unable to unmarshal JSON.*")
    public void testGetChainrInstanceFromFileSystemWithInstantiator_error()
            throws Exception {
        ChainrFactory.fromFileSystem( fileSystemPath + MALFORMED_INPUT_FILENAME, new DefaultChainrInstantiator() );
    }

    @Test
    public void testGetChainrInstanceFromFile_success()
            throws Exception {
        Chainr result = ChainrFactory.fromFile( wellformedFile );
        Assert.assertNotNull( result, "ChainrFactory did not return an instance of Chainr." );
    }

    @Test( expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Unable to load chainr spec file.*" )
    public void testGetChainrInstanceFromFile_error()
            throws Exception {
        ChainrFactory.fromFile( malformedFile );
    }

    @Test
    public void testGetChainrInstanceFromFileWithInstantiator_success()
            throws Exception {
        Chainr result = ChainrFactory.fromFile( wellformedFile, new DefaultChainrInstantiator() );
        Assert.assertNotNull( result, "ChainrFactory did not return an instance of Chainr." );
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Unable to load chainr spec file.*")
    public void testGetChainrInstanceFromFileWithInstantiator_error()
            throws Exception {
        ChainrFactory.fromFile( malformedFile, new DefaultChainrInstantiator() );
    }

    private String getFileSystemPath() {
        // The horrifying code you see below chooses the "correct" path to the test files to feed to the CLI.
        // When the test is run by maven the working directory is $JOLT_CHECKOUT/tools/. The code figures this out
        // by examining the last directory in the file path for the working directory. If it's 'tools' then it
        // chooses the path to the resource files copied by maven into the target/ directory. Obviously, this assumes
        // that you did not name $JOLT_CHECKOUT 'tools'. If that check fails then the path is chosen with the assumption
        // that the test is running in an IDE (Intellij IDEA in my case). Your mileage with other IDE's may very.
        String path = System.getProperty( "user.dir" );
        if ( path.endsWith( "complete" ) ) {
            // This test is being run by maven
            path += "//target//test-classes//json//";
        } else {
            // This test is being run in an IDE (IntelliJ IDEA)
            path += "//complete//src//test//resources//json//";
        }
        return path;
    }

}
