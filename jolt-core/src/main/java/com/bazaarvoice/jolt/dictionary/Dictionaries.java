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
package com.bazaarvoice.jolt.dictionary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Simple dictionary manager. Keep the dictionary in memory allowing them to be
 * used during transformation. Structure is thread safe, we keep only a copy of
 * the given dictionaries.
 *
 */
public class Dictionaries {

	static final ConcurrentHashMap<String, Map<String, String>> dictionaries = new ConcurrentHashMap<>();

	/**
	 * Safely set a dictionary. Keep a full copy of the dictionary in a
	 * concurrentHashMap avoiding any issue during usage.
	 * 
	 * @param name
	 * @param dictionary
	 */
	public static void setDictionary(String name, Map<String, String> dictionary) {
		dictionaries.put(name, new ConcurrentHashMap<String, String>(dictionary));
	}

	/**
	 * unsafe set of the dictionary. A reference is kept and use. Concurrent
	 * exception may occur if not handled properly. But this allow to have
	 * distributed cache such as EHCache which is implementing the Map interface.
	 * 
	 * @param name
	 * @param dictionary
	 */
	public static void setUnsafeDictionary(String name, Map<String, String> dictionary) {
		dictionaries.put(name, dictionary);
	}

	/**
	 * Remove a dictionary from memory.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean removeDictionary(String name) {

		return (dictionaries.remove(name) != null);
	}

	/**
	 * Remove all the dictionary from memory.
	 */
	public static void removeAll() {

		dictionaries.clear();
	}

	public Dictionaries() {
		// TODO Auto-generated constructor stub
	}

	public static Map<String, String> getDictionary(String dictionary) {
		return dictionaries.get(dictionary);
	}

}
