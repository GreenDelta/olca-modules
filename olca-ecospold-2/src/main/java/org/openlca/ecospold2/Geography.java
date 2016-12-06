package org.openlca.ecospold2;

import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class Geography {

	public String id;
	public String shortName;
	public String comment;

	static Geography fromXml(Element e) {
		if (e == null)
			return null;
		Geography geography = new Geography();
		geography.id = e.getAttributeValue("geographyId");
		List<Element> comments = In.childs(e, "comment", "text");
		geography.comment = In.joinText(comments);
		geography.shortName = In.childText(e, "shortname");
		return geography;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element element = new Element("geography", ns);
		if (id != null)
			element.setAttribute("geographyId", id);
		if (shortName != null)
			Out.addChild(element, "shortname", shortName);
		if (comment != null) {
			Element commentElement = Out.addChild(element, "comment");
			Out.addIndexedText(commentElement, comment);
		}
		return element;
	}

}
