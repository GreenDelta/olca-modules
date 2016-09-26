package org.openlca.ilcd.descriptors;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StringMultiLang", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI", propOrder = { "value" })
public class LangString implements Serializable {

	private final static long serialVersionUID = 1L;

	@XmlValue
	public String value;

	@XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
	public String lang;

}
