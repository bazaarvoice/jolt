package com.bazaarvoice.jolt.dictionary;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Dictionaries {

	static ConcurrentHashMap<String, Map<String, String>> dictionaries = new ConcurrentHashMap<>();

	public static void setDictionary(String name, Map<String, String> dictionary) {
		dictionaries.put(name, new ConcurrentHashMap<String, String>(dictionary));
	}

	public static boolean removeDictionary(String name) {

		return (dictionaries.remove(name) != null);
	}

	public Dictionaries() {
		// TODO Auto-generated constructor stub
	}

	public static Map<String, String> getDictionary(String dictionary) {
		return dictionaries.get(dictionary);
	}

}
