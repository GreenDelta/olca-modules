package org.openlca.ilcd.commons;

/**
 * Interface for multi-language strings. A multi-language string is simply a
 * string with a language code.
 */
public interface ILangString {

	/** Get the language code of the string. */
	String getLang();

	/** Set the language code of the string. */
	void setLang(String code);

	/** Get the value of the string. */
	String getValue();

	/** Set the value of the string. */
	void setValue(String value);

}
