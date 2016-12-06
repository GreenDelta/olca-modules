package org.openlca.ecospold2;

import java.util.List;

import org.jdom2.Element;

public class Technology {

	public Integer technologyLevel;
	public String comment;

	Element toXml() {
		Element element = new Element("technology", IO.NS);
		if (technologyLevel != null)
			element.setAttribute("technologyLevel", technologyLevel.toString());
		if (comment != null) {
			Element commentElement = Out.addChild(element, "comment");
			Out.addIndexedText(commentElement, comment);
		}
		return element;
	}

	static Technology fromXml(Element element) {
		if (element == null)
			return null;
		Technology tech = new Technology();
		String levelStr = element.getAttributeValue("technologyLevel");
		if (levelStr != null)
			tech.technologyLevel = Integer.parseInt(levelStr);
		List<Element> comments = In.childs(element, "comment", "text");
		tech.comment = In.joinText(comments);
		return tech;
	}
}
