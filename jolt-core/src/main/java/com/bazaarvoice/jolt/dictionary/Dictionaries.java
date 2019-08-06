package com.bazaarvoice.jolt.dictionary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * Simple dictionary manager. Keep the dictionary in memory allowing them to be used during transformation.
 * Structure is thread safe, we keep only a copy of the given dictionaries. 
 *
 */
public class Dictionaries {

	static ConcurrentHashMap<String, Map<String, String>> dictionaries = new ConcurrentHashMap<>();

	public static void setDictionary(String name, Map<String, String> dictionary) {
		dictionaries.put(name, new ConcurrentHashMap<String, String>(dictionary));
	}

	public static boolean removeDictionary(String name) {

		return (dictionaries.remove(name) != null);
	}

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
