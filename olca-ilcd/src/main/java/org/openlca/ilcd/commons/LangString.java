package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

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

	@Override
	public String toString() {
		String v = value != null ? value : "";
		if (lang == null)
			return v;
		return v + "@" + lang;
	}

	@Override
	public LangString clone() {
		return LangString.of(value, lang);
	}

	/**
	 * Copies all language strings from the source list to the target list.
	 */
	public static void copy(List<LangString> source, List<LangString> target) {
		if (source == null || target == null)
			return;
		for (LangString s : source) {
			target.add(s.clone());
		}
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
	 * Get the first string value from the given list that matches a given
	 * language code. The language codes are checked from left to right. If
	 * there is no string with the given code in the list, the value of the
	 * first language string will be returned. If the given list is empty, null
	 * will be returned.
	 */
	public static String getFirst(List<LangString> list, String... langs) {
		if (list == null || list.isEmpty())
			return null;
		if (langs == null)
			return list.get(0).value;
		for (String lang : langs) {
			LangString s = get(list, lang);
			if (s != null)
				return s.value;
		}
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
		} else if (value != null) {
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
