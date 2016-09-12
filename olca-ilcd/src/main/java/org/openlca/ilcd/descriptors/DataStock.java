package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataStock {

	@XmlAttribute(name = "root", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public boolean root;

	@XmlElement(name = "uuid", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String uuid;

	@XmlElement(name = "shortName", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString shortName;

	@XmlElement(name = "name", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString name;

	@XmlElement(name = "description", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString description;

}
