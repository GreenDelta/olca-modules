package org.openlca.io.maps.content;

import org.openlca.io.KeyGen;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.process.ElementaryExchangeRow;

public class SPElementaryFlowContent implements IMappingContent {

	private String name;
	private ElementaryFlowType flowType;
	private String subCompartment;
	private String unit;
	private String casNumber;

	private double conversionFactor;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ElementaryFlowType getFlowType() {
		return flowType;
	}

	public void setFlowType(ElementaryFlowType flowType) {
		this.flowType = flowType;
	}

	public String getSubCompartment() {
		return subCompartment;
	}

	public void setSubCompartment(String subCompartment) {
		this.subCompartment = subCompartment;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getCasNumber() {
		return casNumber;
	}

	public void setCasNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public ElementaryExchangeRow toRow() {
		ElementaryExchangeRow row = new ElementaryExchangeRow();
		row.setSubCompartment(subCompartment);
		row.setName(name);
		row.setUnit(unit);
		return row;
	}

	@Override
	public String getKey() {
		return KeyGen.get(flowType != null ? flowType.getExchangeHeader()
				: null, name, subCompartment, unit);
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		SPElementaryFlowContent other = (SPElementaryFlowContent) obj;
		return this.getKey().equals(other.getKey());
	}
}
