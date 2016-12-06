package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Technology {

	@XmlAttribute(name = "technologyLevel")
	public Integer level;

	public RichText comment;

	Element toXml() {
		Element e = new Element("technology", IO.NS);
		if (level != null)
			e.setAttribute("technologyLevel", level.toString());
		if (comment != null) {
			Element commentElement = Out.addChild(e, "comment");
			Out.fill(commentElement, comment);
		}
		return e;
	}

	static Technology fromXml(Element e) {
		if (e == null)
			return null;
		Technology tech = new Technology();
		String levelStr = e.getAttributeValue("technologyLevel");
		if (levelStr != null)
			tech.level = Integer.parseInt(levelStr);
		tech.comment = In.richText(e, "comment");
		return tech;
	}
}
