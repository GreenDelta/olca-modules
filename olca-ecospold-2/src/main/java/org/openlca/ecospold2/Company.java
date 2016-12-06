package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Company {

	public String id;
	public String code;
	public String website;
	public String name;
	public String comment;

	Element toXml(Namespace ns) {
		Element element = new Element("company", ns);
		if (id != null)
			element.setAttribute("id", id);
		if (code != null)
			element.setAttribute("code", code);
		if (website != null)
			element.setAttribute("website", website);
		if (name != null)
			Out.addChild(element, "name", name);
		if (comment != null)
			Out.addChild(element, "comment", comment);
		return element;
	}

}
