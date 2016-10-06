package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataStock {

	@XmlAttribute(name = "root")
	public boolean root;

	@XmlElement(name = "uuid")
	public String uuid;

	@XmlElement(name = "shortName")
	public LangString shortName;

	@XmlElement(name = "name")
	public LangString name;

	@XmlElement(name = "description")
	public LangString description;

	@Override
	public String toString() {
		String name = shortName != null ? shortName.value : "";
		return "DataStock [ " + name + "/" + uuid + "/root=" + root + "]";
	}
}
