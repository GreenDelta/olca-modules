package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiProduct {

	@XmlAttribute(name = "version")
	String version;

	@XmlAttribute(name = "lang")
	String lang;

	@XmlValue
	String value;

	public String version() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String lang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String value() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
