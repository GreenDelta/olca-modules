package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import org.openlca.ilcd.util.IlcdConfig;

@XmlAccessorType(XmlAccessType.FIELD)
public class LangString implements Serializable {

	private static final long serialVersionUID = -818083869232748527L;

	@XmlValue
	public String value;

	@XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
	public String lang;

	public static LangString of(String value, String lang) {
		LangString s = new LangString();
		s.value = value;
		s.lang = lang;
		return s;
	}

	/**
	 * Returns the language string with the given language code from the list.
	 * Returns null if there is no such string in the list.
	 */
	public static LangString get(List<LangString> list, String lang) {
		if (list == null)
			return null;
		for (LangString s : list) {
			if (equal(s.lang, lang))
				return s;
		}
		return null;
	}

	/**
	 * Returns the value of language string with the given language code from
	 * the list. Returns null if there is no such string in the list.
	 */
	public static String getVal(List<LangString> list, String lang) {
		LangString s = get(list, lang);
		return s == null ? null : s.value;
	}

	/**
	 * Get the string value from the given list that best matches the given
	 * language configuration.
	 */
	public static String getVal(List<LangString> list, IlcdConfig conf) {
		if (conf == null || list == null || list.isEmpty())
			return null;
		String s = getVal(list, conf.preferredLanguage);
		if (s != null)
			return s;
		s = getVal(list, conf.defaultLanguage);
		if (s != null)
			return s;
		return list.get(0).value;
	}

	/**
	 * If there is already a language string with the given language code in the
	 * list update this language string, otherwise create a new language string
	 * and add it to the list.
	 */
	public static void set(List<LangString> list, String value, String lang) {
		LangString s = get(list, lang);
		if (s != null) {
			s.value = value;
		} else {
			s = LangString.of(value, lang);
			list.add(s);
		}
	}

	private static boolean equal(String s1, String s2) {
		if (empty(s1) && empty(s2))
			return true;
		if (empty(s1) || empty(s2))
			return false;
		return s1.trim().equalsIgnoreCase(s2.trim());
	}

	private static boolean empty(String s) {
		if (s == null)
			return true;
		return s.trim().length() == 0;
	}

}
