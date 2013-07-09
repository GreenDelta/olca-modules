package org.openlca.ecospold2;

import org.jdom2.Element;

public class ElementaryExchange extends Exchange {

	private String elementaryExchangeId;
	private Compartment compartment;

	public String getElementaryExchangeId() {
		return elementaryExchangeId;
	}

	public void setElementaryExchangeId(String elementaryExchangeId) {
		this.elementaryExchangeId = elementaryExchangeId;
	}

	public void setCompartment(Compartment compartment) {
		this.compartment = compartment;
	}

	public Compartment getCompartment() {
		return compartment;
	}

	static ElementaryExchange fromXml(Element e) {
		if (e == null)
			return null;
		ElementaryExchange exchange = new ElementaryExchange();
		exchange.setElementaryExchangeId(e
				.getAttributeValue("elementaryExchangeId"));
		exchange.readValues(e);
		exchange.setCompartment(Compartment.fromXml(In.child(e, "compartment")));
		return exchange;
	}

	Element toXml() {
		Element element = new Element("elementaryExchange", Out.NS);
		writeValues(element);
		element.setAttribute("elementaryExchangeId", elementaryExchangeId);
		if (compartment != null)
			element.addContent(compartment.toXml());
		writeInputOutputGroup(element);
		return element;
	}

}
