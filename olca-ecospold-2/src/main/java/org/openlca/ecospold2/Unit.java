package org.openlca.ecospold2;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Unit {

	private String id;
	private String name;
	private String comment;

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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	static Unit fromXml(Element e) {
		if (e == null)
			return null;
		Unit unit = new Unit();
		unit.id = e.getAttributeValue("id");
		unit.name = In.childText(e, "name");
		unit.comment = In.childText(e, "comment");
		return unit;
	}

	Element toXml(Namespace ns) {
		Element element = new Element("unit", ns);
		if (id != null)
			element.setAttribute("id", id);

		if (name != null)
			Out.addChild(element, "name", name);

		if (comment != null)
			Out.addChild(element, "comment", comment);

		return element;
	}
}
