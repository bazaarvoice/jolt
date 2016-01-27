package com.bazaarvoice.jolt;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.bazaarvoice.jolt.convertr.Key;
import com.bazaarvoice.jolt.exception.TransformException;

public class Convertr implements SpecDriven, Transform{
	public interface WildCards {
        public static final String STAR = "*";
        public static final String OR = "|";
        public static final String ARRAY = "[]";
    }

    private final Key mapRoot;
    private final Key arrayRoot;
	
	@Inject
	public Convertr(Object spec) {
		String rootString = "root";

        {
            Map<String, Object> rootSpec = new LinkedHashMap<>();
            rootSpec.put( rootString, spec );
            mapRoot = Key.parseSpec( rootSpec ).iterator().next();
        }

        {
            Map<String, Object> rootSpec = new LinkedHashMap<>();
            rootSpec.put( rootString + WildCards.ARRAY, spec );
            Key tempKey = null;
            try {
                tempKey = Key.parseSpec( rootSpec ).iterator().next();
            }
            catch ( NumberFormatException nfe ) {
                // this is fine, it means the top level spec has non numeric keys
                // if someone passes a top level array as input later we will error then
            }
            arrayRoot = tempKey;
        }
	}

	public Object transform(Object input) {
		if ( input == null ) {
            // if null, assume HashMap
            input = new HashMap();
        }

        if ( input instanceof List ) {
            if  ( arrayRoot == null ) {
                throw new TransformException( "The Spec provided can not handle input that is a top level Json Array." );
            }
            arrayRoot.applyChildren( input );
        }
        else {
            mapRoot.applyChildren( input );
        }

        return input;
	}
}
