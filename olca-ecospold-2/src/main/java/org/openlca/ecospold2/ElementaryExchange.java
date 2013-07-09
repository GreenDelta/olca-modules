package org.openlca.ecospold2;

import org.jdom2.Element;

public class ElementaryExchange extends Exchange {

	private String elementaryExchangeId;

	public String getElementaryExchangeId() {
		return elementaryExchangeId;
	}

	public void setElementaryExchangeId(String elementaryExchangeId) {
		this.elementaryExchangeId = elementaryExchangeId;
	}

	static ElementaryExchange fromXml(Element e) {
		if (e == null)
			return null;
		ElementaryExchange exchange = new ElementaryExchange();
		exchange.setElementaryExchangeId(e
				.getAttributeValue("elementaryExchangeId"));
		exchange.readValues(e);
		return exchange;
	}

	Element toXml() {
		Element element = new Element("elementaryExchange", Out.NS);
		element.setAttribute("elementaryExchangeId", elementaryExchangeId);
		writeValues(element);
		return element;
	}

}
