package com.bazaarvoice.jolt;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

public class DiffyTool {

    private static Diffy diffy = new Diffy();

    public static void main (String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser( "diffy" )
                .description( "Jolt CLI Diffy Tool" )
                .defaultHelp( true );
        parser.addArgument( "filePath1" ).help( "File path to first JSON document to be fed to Diffy" );
        parser.addArgument( "filePath2" ).help( "File path to second JSON document to be fed to Diffy" );

        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        String filePath1 = ns.getString( "filePath1" );
        String filePath2 = ns.getString( "filePath2" );

        Map<String, Object> objectMap1;
        Map<String, Object> objectMap2;
        try {

            FileInputStream inputStream = new FileInputStream( filePath1 );
            objectMap1 = (Map<String, Object>) JsonUtils.jsonToObject( inputStream );
            inputStream.close();

            inputStream = new FileInputStream( filePath2 );
            objectMap2 = (Map<String, Object>) JsonUtils.jsonToObject( inputStream );
            inputStream.close();

            Diffy.Result result = diffy.diff( objectMap1, objectMap2 );

            System.out.println( "result.isEmpty(): " + result.isEmpty() );
            System.out.println( "result.expected: " + JsonUtils.toPrettyJsonString( result.expected ) );
            System.out.println( "result.actual: " + JsonUtils.toPrettyJsonString( result.actual ) );
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
}
