package org.openlca.ilcd.descriptors;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.openlca.ilcd.commons.LangString;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataStock {

	@XmlAttribute(name = "root")
	public boolean root;

	@XmlElement(name = "uuid", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String uuid;

	@XmlElement(name = "shortName", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public String shortName;

	@XmlElement(name = "name", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString name;

	@XmlElement(name = "description", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public LangString description;

	/**
	 * Contains the user roles. Is only used when the data stock description is
	 * returned in authentication information.
	 */
	@XmlElement(name = "role", namespace = "http://www.ilcd-network.org/ILCD/ServiceAPI")
	public final List<String> roles = new ArrayList<>();

	@Override
	public String toString() {
		return "DataStock [ " + shortName + "/" + uuid + "/root=" + root + "]";
	}

	public boolean isReadAllowed() {
		return this.roles.contains("READ");
	}

	public boolean isExportAllowed() {
		return this.roles.contains("EXPORT");
	}

}
