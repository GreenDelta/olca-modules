package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class DataEntry {

	@XmlAttribute
	public String personId;

	@XmlAttribute
	public Boolean isActiveAuthor;

	@XmlAttribute
	public String personName;

	@XmlAttribute
	public String personEmail;

	static DataEntry fromXml(Element e) {
		if (e == null)
			return null;
		DataEntry entry = new DataEntry();
		entry.personId = e.getAttributeValue("personId");
		String authStr = e.getAttributeValue("isActiveAuthor");
		if (authStr != null)
			entry.isActiveAuthor = In.bool(authStr);
		entry.personName = e.getAttributeValue("personName");
		entry.personEmail = e.getAttributeValue("personEmail");
		return entry;
	}

	Element toXml() {
		Element element = new Element("dataEntryBy", IO.NS);
		if (personId != null)
			element.setAttribute("personId", personId);
		if (isActiveAuthor != null)
			element.setAttribute("isActiveAuthor", isActiveAuthor.toString());
		if (personName != null)
			element.setAttribute("personName", personName);
		if (personEmail != null)
			element.setAttribute("personEmail", personEmail);
		return element;
	}

}
