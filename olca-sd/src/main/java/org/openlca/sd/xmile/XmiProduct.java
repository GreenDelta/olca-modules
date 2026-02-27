package org.openlca.sd.xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiProduct {

	@XmlAttribute(name = "version")
	private String version;

	@XmlAttribute(name = "lang")
	private String lang;

	@XmlValue
	private String value;

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
