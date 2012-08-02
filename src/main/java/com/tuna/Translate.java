package com.tuna;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.OutputStreamWriter;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;

public class Translate {

    public static void main( String [] args ) {

        System.out.println( "Hello World." );



        // Add the values in the datamodel
        Map datamodel = new HashMap();
        datamodel.put("pet", "Bunny");
        datamodel.put("number", new Integer(6));

        // Process the template using FreeMarker
        try {
            freemarkerDo(datamodel, args[1]);
        }
        catch(Exception e) {
            System.out.println(e.getLocalizedMessage());
        }
    }

    // Process a template using FreeMarker and print the results
    static void freemarkerDo(Map datamodel, String template) throws Exception
    {
        Configuration cfg = new Configuration();
        Template tpl = cfg.getTemplate(template);
        OutputStreamWriter output = new OutputStreamWriter(System.out);

        tpl.process(datamodel, output);
    }
}