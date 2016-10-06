package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.openlca.ilcd.commons.LangString;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataStock {

	@XmlAttribute(name = "root")
	public boolean root;

	@XmlElement(name = "uuid")
	public String uuid;

	@XmlElement(name = "shortName")
	public String shortName;

	@XmlElement(name = "name")
	public LangString name;

	@XmlElement(name = "description")
	public LangString description;

	@Override
	public String toString() {
		return "DataStock [ " + shortName + "/" + uuid + "/root=" + root + "]";
	}
}
