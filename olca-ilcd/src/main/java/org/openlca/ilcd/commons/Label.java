package org.openlca.ilcd.commons;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Multi-lang string with a maximum length of 500 characters.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StringMultiLang", propOrder = { "value" })
public class Label implements Serializable, ILangString {

	private final static long serialVersionUID = 1L;
	@XmlValue
	protected String value;
	@XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
	protected String lang;

	/**
	 * String with a maximum length of 500 characters. Must have a minimum
	 * length of 1.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the value property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Gets the value of the lang property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	@Override
	public String getLang() {
		if (lang == null)
			return "en";
		return lang;
	}

	/**
	 * Sets the value of the lang property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	@Override
	public void setLang(String value) {
		this.lang = value;
	}

}
