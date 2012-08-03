package com.tuna;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.String;
import java.lang.System;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class Translate {

    public static void main( String [] args ) throws Exception {

        System.out.println( "Hello World." );

        JsonFactory factory = new JsonFactory();
        ObjectMapper mapper = new ObjectMapper(factory);
        File json = new File(args[0]);
        TypeReference<HashMap<String,Object>> typeRef
                = new TypeReference<HashMap<String,Object>>() {};
        HashMap<String,Object> o = mapper.readValue(json, typeRef);
        System.out.println("Got " + o);

//        mapper.writeValue(System.out, o);
//        System.out.println("\nComplete.");

        // Add the values in the datamodel
//        Map datamodel = new HashMap();
//        datamodel.put("pet", "Bunny");
//        datamodel.put("number", new Integer(6));

//        File ftl = new File(args[1]);


        FileTemplateLoader ftlLoader = new FileTemplateLoader(new File("/Users/msimpson/code/concierge/cli/src/main/resources"));

        Configuration cfg = new Configuration();
        cfg.setTemplateLoader(ftlLoader);

        Template tpl = cfg.getTemplate("tuna.ftl");
        OutputStreamWriter output = new OutputStreamWriter(System.out);

        tpl.process(o, output);

        // Process the template using FreeMarker
    }
}