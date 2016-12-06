package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Person {

	public String id;
	public String name;
	public String address;
	public String telephone;
	public String telefax;
	public String email;
	public String companyId;
	public String companyName;

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

	Element toXml(Namespace namespace) {
		Element element = new Element("person", namespace);
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
