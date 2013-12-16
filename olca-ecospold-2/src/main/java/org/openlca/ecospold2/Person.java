package org.openlca.ecospold2;

import org.jdom2.Element;

public class Person {

	private String id;
	private String name;
	private String address;
	private String telephone;
	private String telefax;
	private String email;
	private String companyId;
	private String companyName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getTelefax() {
		return telefax;
	}

	public void setTelefax(String telefax) {
		this.telefax = telefax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	static Person fromXml(Element e) {
		if (e == null)
			return null;
		Person person = new Person();
		person.id = e.getAttributeValue("id");
		person.name = e.getAttributeValue("name");
		person.address = e.getAttributeValue("address");
		person.telephone = e.getAttributeValue("telephone");
		person.telefax = e.getAttributeValue("telefax");
		person.email = e.getAttributeValue("email");
		person.companyId = e.getAttributeValue("companyId");
		person.companyName = In.childText(e, "companyName");
		return person;
	}

	Element toXml() {
		Element element = new Element("person", IO.NS);

		if (id != null)
			element.setAttribute("id", id);

		if (name != null)
			element.setAttribute("name", name);

		if (address != null)
			element.setAttribute("address", address);

		if (telephone != null)
			element.setAttribute("telephone", telephone);

		if (telefax != null)
			element.setAttribute("telefax", telefax);

		if (email != null)
			element.setAttribute("email", email);

		if (companyId != null)
			element.setAttribute("companyId", companyId);

		if (companyName != null)
			Out.addChild(element, "companyName", companyName);

		return element;
	}

}
