package org.openlca.ecospold2;

import org.jdom2.Element;

public class DataEntryBy {

	public String personId;
	public Boolean isActiveAuthor;
	public String personName;
	public String personEmail;

	static DataEntryBy fromXml(Element e) {
		if (e == null)
			return null;
		DataEntryBy entry = new DataEntryBy();
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
