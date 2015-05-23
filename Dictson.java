/*******************************************************************************
 * Copyright 2015 Thomas Fuchs-Martin.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.appham.dictson;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.OrderedMap;

/**
 * Cross-platform languages manager for libgdx.
 * Reads text values from the json file dictionary.json in the assets/data folder of the libgdx project.
 * 
 * Example structure of the underlying dictionary.json file:
 * {
 * 	"en": [{		
 * 		"key" : "text",
 * 		"another_key" : "another text"
 * 	}],
 * 	
 * 	"es": [{
 * 		"key" : "texto",
 * 		"another_key" : "otro texto"
 * 	}]
 * }
 * 
 * @author donfuxx
 *
 */
public class Dictson {
	private static final Dictson INSTANCE = new Dictson();
	private static final String REPLACEMENT_STR = "#";
	private Locale locale;
	private OrderedMap<String, Array<JsonValue>> wordsMap;

	private Dictson() {}

	/**
	 * Returns a Dictson instance with the text values obtained from dictionary.json file
	 * 
	 * @param aLocale
	 * @return a Dictson instance with the requested Locale or English if not supported
	 */
	@SuppressWarnings("unchecked")
	public static Dictson getInstance(Locale aLocale) {
		INSTANCE.wordsMap = new Json().fromJson(OrderedMap.class, Gdx.files.internal("data/dictionary.json"));
		INSTANCE.setLocale(getSupportedLocale(aLocale));
		return INSTANCE;
	}

	/**
	 * Verifies that the requested Language locale is defined in the OrderedMap that was obtained from dictionary.json.
	 * For the case it is not defined, but a top-level locale has been defined then will return the top-level language.
	 * Example: "es_ES" has been requested and "es" is defined in dictionary.json then "es" will be set as the locale.
	 * By default English locale is returned.
	 * 
	 * @param aLocale
	 * @return the Locale of the most relevant supported language
	 */
	private static Locale getSupportedLocale(Locale aLocale) {
		if (aLocale == null || aLocale.getLanguage().length() < 2) {
			return Locale.ENGLISH;
		}
		if (INSTANCE.wordsMap.containsKey(aLocale.getLanguage().substring(0, 2))) {
			if (INSTANCE.wordsMap.containsKey(aLocale.getLanguage())) {
				return aLocale;
			}
			return new Locale(aLocale.getLanguage().substring(0, 2));
		}
		return Locale.ENGLISH;
	}

	/**
	 * @param key
	 * @return the corresponding text
	 */
	public String get(String key) {
		try {
			return wordsMap.get(locale.getLanguage()).first().getString(key);
		} catch (Exception e) {
			Gdx.app.error("Dictson", e.getMessage());
			return key;
		}
	}

	/**
	 * @param key
	 * @param params
	 * @return the corresponding text with the params inserted in the placeholders
	 */
	public String get(String key, String... params) {
		String text = get(key);
		for (int i = 0; i < params.length; i++) {
			text = text.replaceFirst(REPLACEMENT_STR, params[i].replace("$", "\\$"));
		}
		return text;
	}

	/**
	 * Sets the current (supported) locale
	 * 
	 * @param aLocale
	 */
	public void setLocale(Locale aLocale) {
		this.locale = getSupportedLocale(aLocale);
	}

	/**
	 * @return the current locale
	 */
	public Locale getLocale() {
		return locale;
	}

}
