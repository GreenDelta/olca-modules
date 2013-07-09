package org.openlca.ecospold2;

import org.jdom2.Element;

public class Compartment {

	private String subcompartmentId;
	private String compartment;
	private String subcompartment;

	public String getSubcompartmentId() {
		return subcompartmentId;
	}

	public void setSubcompartmentId(String subcompartmentId) {
		this.subcompartmentId = subcompartmentId;
	}

	public String getCompartment() {
		return compartment;
	}

	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}

	public String getSubcompartment() {
		return subcompartment;
	}

	public void setSubcompartment(String subcompartment) {
		this.subcompartment = subcompartment;
	}

	static Compartment fromXml(Element e) {
		if (e == null)
			return null;
		Compartment compartment = new Compartment();
		compartment
				.setSubcompartmentId(e.getAttributeValue("subcompartmentId"));
		compartment.setCompartment(In.childText(e, "compartment"));
		compartment.setSubcompartment(In.childText(e, "subcompartment"));
		return compartment;
	}

	Element toXml() {
		Element element = new Element("compartment", Out.NS);
		element.setAttribute("subcompartmentId", subcompartmentId);
		Out.addChild(element, "compartment", compartment);
		Out.addChild(element, "subcompartment", subcompartment);
		return element;
	}

}
