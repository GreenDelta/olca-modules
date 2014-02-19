package org.openlca.io.maps.content;

import org.openlca.io.csv.input.CSVKeyGen;
import org.openlca.simapro.csv.model.SPElementaryFlow;
import org.openlca.simapro.csv.model.SPUnit;
import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

public class CSVElementaryFlowContent implements IMappingContent {

	private String name;
	private SPUnit unit;
	private String casNumber;
	private ElementaryFlowType type;
	private SubCompartment subCompartment;
	private double factor;

	public CSVElementaryFlowContent() {
	}

	public CSVElementaryFlowContent(String name, SPUnit unit, String casNumber,
			ElementaryFlowType type, SubCompartment subCompartment) {
		this.name = name;
		this.unit = unit;
		this.casNumber = casNumber;
		this.type = type;
		this.subCompartment = subCompartment;
	}

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
		return new SPElementaryFlow(type, subCompartment, name, unit.getName(),
				"0");
	}

	public void setSubCompartment(SubCompartment subCompartment) {
		this.subCompartment = subCompartment;
	}

	@Override
	public String getKey() {
		return CSVKeyGen.forElementaryFlow(new SPElementaryFlow(type,
				subCompartment, name, unit.getName(), "0"));
	}
}
