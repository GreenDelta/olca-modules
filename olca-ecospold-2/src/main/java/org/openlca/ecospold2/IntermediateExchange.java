package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;

public class IntermediateExchange extends Exchange {

	private String intermediateExchangeId;
	private String activityLinkId;
	private List<Classification> classifications = new ArrayList<>();

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

	public List<Classification> getClassifications() {
		return classifications;
	}

	static IntermediateExchange fromXml(Element e) {
		if (e == null)
			return null;
		IntermediateExchange exchange = new IntermediateExchange();
		exchange.readValues(e);
		exchange.setActivityLinkId(e.getAttributeValue("activityLinkId"));
		exchange.setIntermediateExchangeId(e
				.getAttributeValue("intermediateExchangeId"));
		List<Element> classElements = In.childs(e, "classification");
		for (Element classElement : classElements) {
			Classification classification = Classification
					.fromXml(classElement);
			exchange.classifications.add(classification);
		}
		return exchange;
	}

	Element toXml() {
		Element element = new Element("intermediateExchange", IO.NS);
		if (intermediateExchangeId != null)
			element.setAttribute("intermediateExchangeId",
					intermediateExchangeId);
		if (activityLinkId != null)
			element.setAttribute("activityLinkId", activityLinkId);
		writeValues(element);
		for (Classification classification : classifications)
			element.addContent(classification.toXml());
		writeInputOutputGroup(element);
		return element;
	}

}
