package org.openlca.ilcd.util;

public class IlcdConfig {

	public final String preferredLanguage;
	public final String defaultLanguage;

	public IlcdConfig(String preferredLanguage) {
		this.preferredLanguage = preferredLanguage;
		this.defaultLanguage = "en";
	}

	public static IlcdConfig getDefault() {
		return new IlcdConfig("en");
	}

}
