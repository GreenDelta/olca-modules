package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;
import org.jdom2.Namespace;

@XmlAccessorType(XmlAccessType.FIELD)
public class Geography {

	@XmlAttribute(name = "geographyId")
	public String id;

	@XmlElement(name = "shortname")
	public String shortName;

	public RichText comment;

	static Geography fromXml(Element e) {
		if (e == null)
			return null;
		Geography geography = new Geography();
		geography.id = e.getAttributeValue("geographyId");
		geography.comment = In.richText(e, "comment");
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
			Out.fill(commentElement, comment);
		}
		return element;
	}

}
