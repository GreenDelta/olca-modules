package org.openlca.io.maps.content;

public class ES2ElementaryFlowContent implements IMappingContent {

	private String elementaryExchangeId;
	private String name;
	private String unitId;
	private String unitName;
	private String casNumber;
	private String subCompartmentId;
	private String compartment;
	private String subCompartment;
	private int factor;

	public String getElementaryExchangeId() {
		return elementaryExchangeId;
	}

	public void setElementaryExchangeId(String elementaryExchangeId) {
		this.elementaryExchangeId = elementaryExchangeId;
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

	public String getCasNumber() {
		return casNumber;
	}

	public void setCasNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	public String getSubCompartmentId() {
		return subCompartmentId;
	}

	public void setSubCompartmentId(String subCompartmentId) {
		this.subCompartmentId = subCompartmentId;
	}

	public String getCompartment() {
		return compartment;
	}

	public void setCompartment(String compartment) {
		this.compartment = compartment;
	}

	public String getSubCompartment() {
		return subCompartment;
	}

	public void setSubCompartment(String subCompartment) {
		this.subCompartment = subCompartment;
	}

	public int getFactor() {
		return factor;
	}

	public void setFactor(int factor) {
		this.factor = factor;
	}

	@Override
	public String getKey() {
		return elementaryExchangeId;
	}

}
