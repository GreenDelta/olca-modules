package org.openlca.simapro.csv.model;

import java.util.HashMap;
import java.util.Map;

public class SPReferenceData {

	private SPSystemDescription systemDescription;
	private Map<String, SPLiteratureReference> literatureReferences = new HashMap<>();
	private Map<String, SPQuantity> quantities = new HashMap<>();
	private Map<String, SPUnit> units = new HashMap<>();
	private Map<String, SPSubstance> substances = new HashMap<>();
	private Map<String, SPInputParameter> inputParameters = new HashMap<>();
	private Map<String, SPCalculatedParameter> calculatedParameters = new HashMap<>();

	/**
	 * 
	 * @param key
	 * @param literatureReference
	 * @return true if the given key contains in the map, otherwise false.
	 */
	public boolean add(String key, SPLiteratureReference literatureReference) {
		boolean result = literatureReferences.containsKey(key);
		literatureReferences.put(key, literatureReference);
		return result;
	}

	public void add(SPQuantity quantity) {
		quantities.put(quantity.getName(), quantity);
	}

	public void add(SPUnit unit) {
		StringBuilder builder = new StringBuilder();
		builder.append(unit.getName());
		builder.append(unit.getConversionFactor());
		builder.append(unit.getQuantity());
		builder.append(unit.getReferenceUnit());
		units.put(builder.toString(), unit);
	}

	public void add(SPSubstance substance) {
		StringBuilder builder = new StringBuilder();
		builder.append(substance.getName());
		builder.append(substance.getFlowType().getExchangeHeader());
		builder.append(substance.getReferenceUnit());
		substances.put(builder.toString(), substance);
	}

	public void add(SPInputParameter parameter) {
		inputParameters.put(parameter.getName(), parameter);
	}

	public void add(SPCalculatedParameter parameter) {
		calculatedParameters.put(parameter.getName(), parameter);
	}

	public SPSystemDescription getSystemDescription() {
		return systemDescription;
	}

	public void setSystemDescription(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

	public void setInputParameters(Map<String, SPInputParameter> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public Map<String, SPInputParameter> getInputParameters() {
		return inputParameters;
	}

	public Map<String, SPQuantity> getQuantities() {
		return quantities;
	}

	public void setQuantities(Map<String, SPQuantity> quantities) {
		this.quantities = quantities;
	}

	public Map<String, SPUnit> getUnits() {
		return units;
	}

	public void setUnits(Map<String, SPUnit> units) {
		this.units = units;
	}

	public Map<String, SPSubstance> getSubstances() {
		return substances;
	}

	public void setSubstances(Map<String, SPSubstance> substances) {
		this.substances = substances;
	}

	public Map<String, SPCalculatedParameter> getCalculatedParameters() {
		return calculatedParameters;
	}

	public void setCalculatedParameters(
			Map<String, SPCalculatedParameter> calculatedParameters) {
		this.calculatedParameters = calculatedParameters;
	}

	public Map<String, SPLiteratureReference> getLiteratureReferences() {
		return literatureReferences;
	}

	public void setLiteratureReferences(
			Map<String, SPLiteratureReference> literatureReferences) {
		this.literatureReferences = literatureReferences;
	}

}
