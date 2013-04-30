package org.openlca.ilcd.util;

import java.util.List;

import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.ShortText;

public class LangString {

	public static String defaultLang = "en";

	private LangString() {
	}

	public static Label label(String value) {
		return label(value, defaultLang);
	}

	public static Label label(String value, String lang) {
		Label label = new Label();
		label.setValue(value);
		label.setLang(lang);
		return label;
	}

	public static void addLabel(List<Label> list, String value) {
		list.add(label(value));
	}

	public static void addLabel(List<Label> list, String value, String lang) {
		list.add(label(value, lang));
	}

	public static FreeText freeText(String value) {
		return freeText(value, defaultLang);
	}

	public static FreeText freeText(String value, String lang) {
		FreeText text = new FreeText();
		text.setValue(value);
		text.setLang(lang);
		return text;
	}

	public static void addFreeText(List<FreeText> list, String value) {
		list.add(freeText(value));
	}

	public static void addFreeText(List<FreeText> list, String value,
			String lang) {
		list.add(freeText(value, lang));
	}

	public static ShortText shortText(String value) {
		return shortText(value, defaultLang);
	}

	public static ShortText shortText(String value, String lang) {
		ShortText text = new ShortText();
		text.setValue(value);
		text.setLang(lang);
		return text;
	}

	public static void addShortText(List<ShortText> list, String value) {
		list.add(shortText(value));
	}

	public static void addShortText(List<ShortText> list, String value,
			String lang) {
		list.add(shortText(value, lang));
	}

	public static String getLabel(List<Label> list) {
		if (list == null || list.size() == 0)
			return null;
		for (Label label : list) {
			if (label.getLang() == null || label.getLang().equals(defaultLang)) {
				return label.getValue();
			}
		}
		return list.get(0).getValue();
	}

	public static String getShortText(List<ShortText> list) {
		if (list == null || list.size() == 0)
			return null;
		for (ShortText text : list) {
			if (text.getLang() == null || text.getLang().equals(defaultLang)) {
				return text.getValue();
			}
		}
		return list.get(0).getValue();
	}

	public static String getFreeText(List<FreeText> list) {
		if (list == null || list.size() == 0) {
			return null;
		}
		for (FreeText text : list) {
			if (text.getLang() == null || text.getLang().equals(defaultLang)) {
				return text.getValue();
			}
		}
		return list.get(0).getValue();
	}
}
