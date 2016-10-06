package org.openlca.ilcd.commons;

public class LangConfig {

	public final String preferredLanguage;
	public final String defaultLanguage;

	public LangConfig(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
		this.defaultLanguage = "en";
	}

	public static LangConfig getDefault() {
		return new LangConfig("en");
	}

}
