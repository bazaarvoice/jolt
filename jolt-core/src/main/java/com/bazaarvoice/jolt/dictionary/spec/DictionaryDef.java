package com.bazaarvoice.jolt.dictionary.spec;

import java.util.HashMap;
import java.util.Map;

import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.Transform;
import com.bazaarvoice.jolt.dictionary.Dictionaries;

public class DictionaryDef implements SpecDriven, Transform {

	public DictionaryDef(Object o) {
		if(o instanceof Map) {
			Map spec = (Map)o;
			for(Object dict:spec.keySet()) {
				Object dictionaryO = spec.get(dict);
				if(dictionaryO instanceof Map) {
					Map input = (Map)dictionaryO;
					Map<String,String> dictionary = new HashMap<String, String>(input.size());
					for(Object  k:input.keySet()) {
						dictionary.put(k.toString(), input.get(k).toString());
					}
					Dictionaries.setDictionary(dict.toString(), dictionary);
				}
			}
		}
	}

	@Override
	public Object transform(Object input) {
		// do nothing 
		return input;
	}

}
