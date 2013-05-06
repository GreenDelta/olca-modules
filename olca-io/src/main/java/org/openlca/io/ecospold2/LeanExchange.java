package org.openlca.io.ecospold2;

import org.jdom2.Element;

class LeanExchange {

	static final int ELEMENTARY_FLOW = 0;
	static final int PRODUCT_FLOW = 1;

	private int type;
	private String flowId;
	private String unitId;
	private String unitName;
	private double amount;
	private String name;
	private String compartment;
	private String subCompartment;
	private Integer inputGroup;
	private Integer outputGroup;
	private String activityLinkId;

	private LeanExchange(int type) {
		this.type = type;
	}

	public static LeanExchange create(Element exchangeElement, int type) {
		LeanExchange exchange = new LeanExchange(type);
		exchange.mapData(exchangeElement);
		return exchange;
	}

	private void mapData(Element exchange) {
		if (exchange == null)
			return;
		flowId = exchange.getAttributeValue("elementaryExchangeId");
		if (flowId == null)
			flowId = exchange.getAttributeValue("intermediateExchangeId");
		unitId = exchange.getAttributeValue("unitId");
		activityLinkId = exchange.getAttributeValue("activityLinkId");
		String amountStr = exchange.getAttributeValue("amount");
		if (amountStr != null)
			amount = Double.parseDouble(amountStr);
		name = Jdom.childText(exchange, "name");
		unitName = Jdom.childText(exchange, "unitName");
		Element compartment = Jdom.child(exchange, "compartment");
		compartment(compartment);
		ioGroup(exchange);
	}

	private void compartment(Element compartmentElement) {
		if (compartmentElement == null)
			return;
		compartment = Jdom.childText(compartmentElement, "compartment");
		subCompartment = Jdom.childText(compartmentElement, "subcompartment");
	}

	private void ioGroup(Element exchange) {
		if (exchange == null)
			return;
		String groupStr = Jdom.childText(exchange, "inputGroup");
		if (groupStr != null) {
			inputGroup = Integer.parseInt(groupStr);
			return;
		}
		groupStr = Jdom.childText(exchange, "outputGroup");
		if (groupStr != null)
			outputGroup = Integer.parseInt(groupStr);
	}

	public int getType() {
		return type;
	}

	public String getFlowId() {
		return flowId;
	}

	public String getUnitId() {
		return unitId;
	}

	public String getUnitName() {
		return unitName;
	}

	public double getAmount() {
		return amount;
	}

	public String getName() {
		return name;
	}

	public String getCompartment() {
		return compartment;
	}

	public String getSubCompartment() {
		return subCompartment;
	}

	public Integer getInputGroup() {
		return inputGroup;
	}

	public Integer getOutputGroup() {
		return outputGroup;
	}

	public String getActivityLinkId() {
		return activityLinkId;
	}

}
