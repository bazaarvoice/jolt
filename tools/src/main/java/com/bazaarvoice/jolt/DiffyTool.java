package com.bazaarvoice.jolt;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class DiffyTool {

    public static void main (String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser( "diffy" )
                .description( "Jolt CLI Diffy Tool" );

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        System.out.println( "Hello, World!" );

    }
}
