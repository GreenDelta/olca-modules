package org.openlca.ecospold2;

import org.jdom2.Element;

public class Compartment {

	public String subcompartmentId;
	public String compartment;
	public String subcompartment;

	static Compartment fromXml(Element e) {
		if (e == null)
			return null;
		Compartment compartment = new Compartment();
		compartment.subcompartmentId = e.getAttributeValue("subcompartmentId");
		compartment.compartment = In.childText(e, "compartment");
		compartment.subcompartment = In.childText(e, "subcompartment");
		return compartment;
	}

	Element toXml() {
		Element element = new Element("compartment", IO.NS);
		element.setAttribute("subcompartmentId", subcompartmentId);
		Out.addChild(element, "compartment", compartment);
		Out.addChild(element, "subcompartment", subcompartment);
		return element;
	}

}
