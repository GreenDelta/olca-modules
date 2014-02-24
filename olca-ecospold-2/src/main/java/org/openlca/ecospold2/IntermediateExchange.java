package org.openlca.ecospold2;

import java.util.ArrayList;
import java.util.List;

import org.jdom2.Element;
import org.jdom2.Namespace;

public class IntermediateExchange extends Exchange {

	private String intermediateExchangeId;
	private String activityLinkId;
	private Double productionVolumeAmount;
	private String productionVolumeVariableName;
	private String productionVolumeMathematicalRelation;
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

	public Double getProductionVolumeAmount() {
		return productionVolumeAmount;
	}

	public void setProductionVolumeAmount(Double productionVolumeAmount) {
		this.productionVolumeAmount = productionVolumeAmount;
	}

	public String getProductionVolumeVariableName() {
		return productionVolumeVariableName;
	}

	public void setProductionVolumeVariableName(
			String productionVolumeVariableName) {
		this.productionVolumeVariableName = productionVolumeVariableName;
	}

	public String getProductionVolumeMathematicalRelation() {
		return productionVolumeMathematicalRelation;
	}

	public void setProductionVolumeMathematicalRelation(
			String productionVolumeMathematicalRelation) {
		this.productionVolumeMathematicalRelation = productionVolumeMathematicalRelation;
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
		exchange.setProductionVolumeAmount(In.optionalDecimal(e
				.getAttributeValue("productionVolumeAmount")));
		exchange.setProductionVolumeMathematicalRelation(e
				.getAttributeValue("productionVolumeMathematicalRelation"));
		exchange.setProductionVolumeVariableName(e
				.getAttributeValue("productionVolumeVariableName"));
		List<Element> classElements = In.childs(e, "classification");
		for (Element classElement : classElements) {
			Classification classification = Classification
					.fromXml(classElement);
			exchange.classifications.add(classification);
		}
		return exchange;
	}

	Element toXml() {
		return toXml(IO.NS);
	}

	Element toXml(Namespace ns) {
		Element element = new Element("intermediateExchange", ns);
		if (intermediateExchangeId != null)
			element.setAttribute("intermediateExchangeId",
					intermediateExchangeId);
		if (activityLinkId != null)
			element.setAttribute("activityLinkId", activityLinkId);
		if (productionVolumeAmount != null)
			element.setAttribute("productionVolumeAmount",
					productionVolumeAmount.toString());
		if (productionVolumeMathematicalRelation != null)
			element.setAttribute("productionVolumeMathematicalRelation",
					productionVolumeMathematicalRelation);
		if (productionVolumeVariableName != null)
			element.setAttribute("productionVolumeVariableName",
					productionVolumeVariableName);
		writeValues(element);
		for (Classification classification : classifications)
			element.addContent(classification.toXml(ns));
		writeInputOutputGroup(element);
		return element;
	}

}
