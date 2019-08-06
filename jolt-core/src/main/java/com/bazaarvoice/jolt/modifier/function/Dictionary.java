package com.bazaarvoice.jolt.modifier.function;

import java.util.Map;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.dictionary.Dictionaries;

public class Dictionary {

	
	public static final class convert extends Function.ArgDrivenSingleFunction<String, String> {
		@Override
		protected Optional<String> applySingle(final String dictionary, final Object source) {
			if (source == null || dictionary == null) {
				return Optional.empty();
			} else if (source instanceof String) {
				// only try to split input strings
				String inputString = (String) source;
				Map<String, String> dict = Dictionaries.getDictionary(dictionary);
				if (dict != null) {
					String out = dict.get(inputString);
					if (out != null) {
						return Optional.of(out);
					}
				}
			}

			return Optional.empty();
		}
	}
}
