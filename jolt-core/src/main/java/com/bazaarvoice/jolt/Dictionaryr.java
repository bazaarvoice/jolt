package com.bazaarvoice.jolt;

import java.util.HashMap;
import java.util.Map;

import com.bazaarvoice.jolt.dictionary.Dictionaries;
import com.bazaarvoice.jolt.exception.SpecException;

/**
 * Allow to specify static global dictionaries in the spec. Format is very
 * simple "dictionary_name" : { "key":"value","key":"value".....},..... Those
 * dictionaries are global and are updated globally ( even if we can think that
 * updating dictionaries on a more granular fashion can be considered as a
 * future evolution ).
 */
public class Dictionaryr implements SpecDriven, Transform {

	public Dictionaryr(Object o) {
		if (o instanceof Map) {
			Map spec = (Map) o;
			for (Object dict : spec.keySet()) {
				Object dictionaryO = spec.get(dict);
				if (dictionaryO instanceof Map) {
					Map input = (Map) dictionaryO;
					Map<String, String> dictionary = new HashMap<String, String>(input.size());
					for (Object k : input.keySet()) {
						dictionary.put(k.toString(), input.get(k).toString());
					}
					Dictionaries.setDictionary(dict.toString(), dictionary);
				}
			}
		} else {
			throw new SpecException(
					"Dictionaryr expected a spec of Map type, got :" + o == null ? "NULL" : o.getClass().getName());
		}
	}

	@Override
	public Object transform(Object input) {
		// do nothing
		return input;
	}

}
