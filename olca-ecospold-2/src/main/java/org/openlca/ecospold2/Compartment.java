package org.openlca.ecospold2;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.jdom2.Element;

@XmlAccessorType(XmlAccessType.FIELD)
public class Compartment {

	@XmlAttribute(name = "subcompartmentId")
	public String id;

	@XmlElement
	public String compartment;

	@XmlElement(name = "subcompartment")
	public String subCompartment;

	static Compartment fromXml(Element e) {
		if (e == null)
			return null;
		Compartment compartment = new Compartment();
		compartment.id = e.getAttributeValue("subcompartmentId");
		compartment.compartment = In.childText(e, "compartment");
		compartment.subCompartment = In.childText(e, "subcompartment");
		return compartment;
	}

	Element toXml() {
		Element element = new Element("compartment", IO.NS);
		element.setAttribute("subcompartmentId", id);
		Out.addChild(element, "compartment", compartment);
		Out.addChild(element, "subcompartment", subCompartment);
		return element;
	}

}
