package org.openlca.io.maps.content;

public class ES2ProductFlowContent implements IMappingContent {

	private String intermediateExchangeId;
	private String name;
	private String unitId;
	private String unitName;

	public String getIntermediateExchangeId() {
		return intermediateExchangeId;
	}

	public void setIntermediateExchangeId(String intermediateExchangeId) {
		this.intermediateExchangeId = intermediateExchangeId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUnitId() {
		return unitId;
	}

	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	@Override
	public String getKey() {
		return intermediateExchangeId;
	}

}
