/*
 * Copyright 2019 Infovista.
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
