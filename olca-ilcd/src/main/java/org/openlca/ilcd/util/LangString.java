package org.openlca.ilcd.util;

import java.util.List;
import java.util.Objects;

import org.openlca.ilcd.commons.FreeText;
import org.openlca.ilcd.commons.ILangString;
import org.openlca.ilcd.commons.Label;
import org.openlca.ilcd.commons.ShortText;

/**
 * Provides helper methods for ILCD multi-language strings.
 */
public final class LangString {

	private LangString() {
	}

	public static Label label(String value, IlcdConfig config) {
		Label label = new Label();
		label.setValue(value);
		label.setLang(config.preferredLanguage);
		return label;
	}

	public static void addLabel(List<Label> list, String value,
			IlcdConfig config) {
		list.add(label(value, config));
	}

	public static FreeText freeText(String value, IlcdConfig config) {
		FreeText text = new FreeText();
		text.setValue(value);
		text.setLang(config.preferredLanguage);
		return text;
	}

	public static void addFreeText(List<FreeText> list, String value,
			IlcdConfig config) {
		list.add(freeText(value, config));
	}

	public static ShortText shortText(String value, IlcdConfig config) {
		ShortText text = new ShortText();
		text.setValue(value);
		text.setLang(config.preferredLanguage);
		return text;
	}

	public static void addShortText(List<ShortText> list, String value,
			IlcdConfig config) {
		list.add(shortText(value, config));
	}

	public static String get(List<? extends ILangString> list, IlcdConfig config) {
		if (list == null || list.isEmpty())
			return null;
		ILangString defaultValue = null;
		for (ILangString string : list)
			if (Objects.equals(string.getLang(), config.preferredLanguage))
				return string.getValue();
			else if (Objects.equals(string.getLang(), config.defaultLanguage))
				defaultValue = string;
		if (defaultValue == null)
			defaultValue = list.get(0);
		return defaultValue.getValue();
	}

}
