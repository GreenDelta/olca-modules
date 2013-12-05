package org.openlca.ecospold2;

import org.jdom2.Element;

public class ElementaryExchange extends Exchange {

	private String elementaryExchangeId;
	private Compartment compartment;
	private String formula;

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

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public String getFormula() {
		return formula;
	}

	static ElementaryExchange fromXml(Element e) {
		if (e == null)
			return null;
		ElementaryExchange exchange = new ElementaryExchange();
		exchange.elementaryExchangeId = e
				.getAttributeValue("elementaryExchangeId");
		exchange.formula = e.getAttributeValue("formula");
		exchange.readValues(e);
		exchange.setCompartment(Compartment.fromXml(In.child(e, "compartment")));
		return exchange;
	}

	Element toXml() {
		Element element = new Element("elementaryExchange", Out.NS);
		writeValues(element);
		if (elementaryExchangeId != null)
			element.setAttribute("elementaryExchangeId", elementaryExchangeId);
		if (compartment != null)
			element.addContent(compartment.toXml());
		if (formula != null)
			element.setAttribute("formula", formula);
		writeInputOutputGroup(element);
		return element;
	}

}
