package org.openlca.ecospold2;

import org.jdom2.Element;

public class IntermediateExchange extends Exchange {

	private String intermediateExchangeId;
	private String activityLinkId;

	public String getIntermediateExchangeId() {
		return intermediateExchangeId;
	}

	public void setIntermediateExchangeId(String intermediateExchangeId) {
		this.intermediateExchangeId = intermediateExchangeId;
	}

	public String getActivityLinkId() {
		return activityLinkId;
	}

	public void setActivityLinkId(String activityLinkId) {
		this.activityLinkId = activityLinkId;
	}

	static IntermediateExchange fromXml(Element e) {
		if (e == null)
			return null;
		IntermediateExchange exchange = new IntermediateExchange();
		exchange.readValues(e);
		exchange.setActivityLinkId(e.getAttributeValue("activityLinkId"));
		exchange.setIntermediateExchangeId(e
				.getAttributeValue("intermediateExchangeId"));
		return exchange;
	}

	Element toXml() {
		Element element = new Element("intermediateExchange", Out.NS);
		element.setAttribute("intermediateExchangeId", intermediateExchangeId);
		if (activityLinkId != null)
			element.setAttribute("activityLinkId", activityLinkId);
		writeValues(element);
		return element;
	}

}
