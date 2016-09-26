package org.openlca.ilcd.commons;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

/**
 * Free text with an unlimited length.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FTMultiLang", propOrder = { "value" })
public class FreeText implements Serializable, ILangString {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String value;

	@XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
	public String lang;

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
