package org.openlca.io.maps.content;

import org.openlca.io.csv.input.CSVKeyGen;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;
import org.openlca.simapro.csv.model.enums.SubCompartment;

public class SPElementaryFlowContent implements IMappingContent {

	private String name;
	private String compartment;
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

	public SPUnit getUnit() {
		return unit;
	}

	public void setUnit(SPUnit unit) {
		this.unit = unit;
	}

	public String getCasNumber() {
		return casNumber;
	}

	public void setCasNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	public ElementaryFlowType getType() {
		return type;
	}

	public void setType(ElementaryFlowType type) {
		this.type = type;
	}

	public SubCompartment getSubCompartment() {
		return subCompartment;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	public SPElementaryFlow createFlow() {
		SPElementaryFlow flow = new SPElementaryFlow();
		flow.setType(ElementaryFlowType.forValue(compartment));
		flow.setSubCompartment(subCompartment);
		flow.setName(name);
		flow.setUnit(unit.getName());
		flow.setAmount("0");
		return flow;
	}

	public void setSubCompartment(SubCompartment subCompartment) {
		this.subCompartment = subCompartment;
	}

	@Override
	public String getKey() {
		return CSVKeyGen.forElementaryFlow(createFlow());
	}
}
