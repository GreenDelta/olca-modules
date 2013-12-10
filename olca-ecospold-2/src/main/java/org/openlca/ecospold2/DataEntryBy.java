package org.openlca.ecospold2;

import org.jdom2.Element;

public class DataEntryBy {

	private String personId;
	private Boolean isActiveAuthor;
	private String personName;
	private String personEmail;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public Boolean getIsActiveAuthor() {
		return isActiveAuthor;
	}

	public void setIsActiveAuthor(Boolean isActiveAuthor) {
		this.isActiveAuthor = isActiveAuthor;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public String getPersonEmail() {
		return personEmail;
	}

	public void setPersonEmail(String personEmail) {
		this.personEmail = personEmail;
	}

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
		Element element = new Element("dataEntryBy", Out.NS);
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
