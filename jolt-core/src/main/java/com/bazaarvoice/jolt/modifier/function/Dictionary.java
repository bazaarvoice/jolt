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
package com.bazaarvoice.jolt.modifier.function;

import java.util.Map;

import com.bazaarvoice.jolt.common.Optional;
import com.bazaarvoice.jolt.dictionary.Dictionaries;
/**
 * This class contain all the function to use dictionaries during a transformation
 * Possible extension : 
 *     Put a default value if the value is not found 
 *             convert(dictionary,source,default)
 *     Use a regexp pattern to extract the values to convert 
 *            convertRegexp(dictionary,Pattern,replacement,source)
 * @author thanot
 *
 */
public class Dictionary {

	/**
	 * Allow to convert a string into another string using a named dictionary
	 * 
	 *
	 */
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
