package org.openlca.ilcd.descriptors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataStock {

	@XmlAttribute(name = "root",
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	private boolean root;

	@XmlElement(name = "uuid",
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	private String uuid;

	@XmlElement(name = "shortName",
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	private LangString shortName;

	@XmlElement(name = "name",
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	private LangString name;

	@XmlElement(name = "description",
			namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	private LangString description;

	public boolean isRoot() {
		return root;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public LangString getShortName() {
		return shortName;
	}

	public void setShortName(LangString shortName) {
		this.shortName = shortName;
	}

	public LangString getName() {
		return name;
	}

	public void setName(LangString name) {
		this.name = name;
	}

	public LangString getDescription() {
		return description;
	}

	public void setDescription(LangString description) {
		this.description = description;
	}

}
