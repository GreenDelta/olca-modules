package org.openlca.ecospold2;

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

}
