package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;
import org.jdom2.Namespace;

@XmlAccessorType(XmlAccessType.FIELD)
public class Classification {

	@XmlAttribute(name = "classificationId")
	public String id;

	@XmlElement(name = "classificationSystem")
	public String system;

	@XmlElement(name = "classificationValue")
	public String value;

	static Classification fromXml(Element element) {
		if (element == null)
			return null;
		Classification c = new Classification();
		c.id = element.getAttributeValue("classificationId");
		c.system = In.childText(element, "classificationSystem");
		c.value = In.childText(element, "classificationValue");
		return c;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element e = new Element("classification", ns);
		e.setAttribute("classificationId", id);
		Out.addChild(e, "classificationSystem", system);
		Out.addChild(e, "classificationValue", value);
		return e;
	}

}
