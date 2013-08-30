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

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;

/**
 * An interface to describe a subcommand for the Jolt CLI Tool.
 */
public interface JoltCliProcessor {

    /**
     * Initializes the subcommand argument parser.
     *
     * @param subparsers The Subparsers object to attach the new Subparser to
     */
    public void intializeSubCommand( Subparsers subparsers );

    /**
     * This method does the processing of the input which is provided via the Namespace
     *
     * @param ns Namespace which contains parsed commandline arguments
     * @return true if processing was successful
     */
    public boolean process( Namespace ns );

}
