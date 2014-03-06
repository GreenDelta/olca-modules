package org.openlca.simapro.csv.model;

import java.util.HashMap;
import java.util.Map;

import org.openlca.simapro.csv.model.refdata.ElementaryFlowRow;
import org.openlca.simapro.csv.model.refdata.LiteratureReferenceBlock;
import org.openlca.simapro.csv.model.refdata.Quantity;
import org.openlca.simapro.csv.model.refdata.UnitRow;

/*
 * TODO:
 * [Literature reference, 
 * Airborne emissions, 
 * Project Calculated parameters, 
 * Database Input parameters, 
 * Raw materials, 
 * Waterborne emissions, 
 * Quantities, 
 * Units, 
 * Process products + waste flows, 
 * System description, 
 * Project Input parameters, 
 * Database Calculated parameters, 
 * Emissions to soil
 * 
 * ]

 * 
 * 
 */

public class SPReferenceData {

	private SPSystemDescription systemDescription;
	private Map<String, LiteratureReferenceBlock> literatureReferences = new HashMap<>();
	private Map<String, Quantity> quantities = new HashMap<>();
	private Map<String, UnitRow> units = new HashMap<>();
	private Map<String, ElementaryFlowRow> substances = new HashMap<>();
	private Map<String, InputParameterRow> inputParameters = new HashMap<>();
	private Map<String, CalculatedParameterRow> calculatedParameters = new HashMap<>();

	/**
	 * 
	 * @param key
	 * @param literatureReference
	 * @return true if the given key contains in the map, otherwise false.
	 */
	public boolean add(String key, LiteratureReferenceBlock literatureReference) {
		boolean result = literatureReferences.containsKey(key);
		literatureReferences.put(key, literatureReference);
		return result;
	}

	public void add(Quantity quantity) {
		quantities.put(quantity.getName(), quantity);
	}

	public void add(UnitRow unit) {
		StringBuilder builder = new StringBuilder();
		builder.append(unit.getName());
		builder.append(unit.getConversionFactor());
		builder.append(unit.getQuantity());
		builder.append(unit.getReferenceUnit());
		units.put(builder.toString(), unit);
	}

	public void add(ElementaryFlowRow substance) {
		StringBuilder builder = new StringBuilder();
		builder.append(substance.getName());
		builder.append(substance.getReferenceUnit());
		substances.put(builder.toString(), substance);
	}

	public void add(InputParameterRow parameter) {
		inputParameters.put(parameter.getName(), parameter);
	}

	public void add(CalculatedParameterRow parameter) {
		calculatedParameters.put(parameter.getName(), parameter);
	}

	public SPSystemDescription getSystemDescription() {
		return systemDescription;
	}

	public void setSystemDescription(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

	public void setInputParameters(Map<String, InputParameterRow> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public Map<String, InputParameterRow> getInputParameters() {
		return inputParameters;
	}

	public Map<String, Quantity> getQuantities() {
		return quantities;
	}

	public void setQuantities(Map<String, Quantity> quantities) {
		this.quantities = quantities;
	}

	public Map<String, UnitRow> getUnits() {
		return units;
	}

	public void setUnits(Map<String, UnitRow> units) {
		this.units = units;
	}

	public Map<String, ElementaryFlowRow> getSubstances() {
		return substances;
	}

	public void setSubstances(Map<String, ElementaryFlowRow> substances) {
		this.substances = substances;
	}

	public Map<String, CalculatedParameterRow> getCalculatedParameters() {
		return calculatedParameters;
	}

	public void setCalculatedParameters(
			Map<String, CalculatedParameterRow> calculatedParameters) {
		this.calculatedParameters = calculatedParameters;
	}

	public Map<String, LiteratureReferenceBlock> getLiteratureReferences() {
		return literatureReferences;
	}

	public void setLiteratureReferences(
			Map<String, LiteratureReferenceBlock> literatureReferences) {
		this.literatureReferences = literatureReferences;
	}

}
