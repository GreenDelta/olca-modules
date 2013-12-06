package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SPReferenceData {

	private SPSystemDescription systemDescription;
	private Map<String, SPLiteratureReference> literatureReferences = new HashMap<>();
	private List<SPQuantity> quantities = new ArrayList<>();
	private List<SPUnit> units = new ArrayList<>();
	private List<SPSubstance> substances = new ArrayList<>();
	private List<SPInputParameter> inputParameters = new ArrayList<>();
	private List<SPCalculatedParameter> calculatedParameters = new ArrayList<>();

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
		quantities.add(quantity);
	}

	public void add(SPUnit unit) {
		units.add(unit);
	}

	public void add(SPSubstance substance) {
		substances.add(substance);
	}

	public void add(SPInputParameter parameter) {
		inputParameters.add(parameter);
	}

	public void add(SPCalculatedParameter parameter) {
		calculatedParameters.add(parameter);
	}

	public SPSystemDescription getSystemDescription() {
		return systemDescription;
	}

	public void setSystemDescription(SPSystemDescription systemDescription) {
		this.systemDescription = systemDescription;
	}

	public List<SPInputParameter> getInputParameters() {
		return inputParameters;
	}

	public void setInputParameters(List<SPInputParameter> inputParameters) {
		this.inputParameters = inputParameters;
	}

	public List<SPQuantity> getQuantities() {
		return quantities;
	}

	public void setQuantities(List<SPQuantity> quantities) {
		this.quantities = quantities;
	}

	public List<SPUnit> getUnits() {
		return units;
	}

	public void setUnits(List<SPUnit> units) {
		this.units = units;
	}

	public List<SPSubstance> getSubstances() {
		return substances;
	}

	public void setSubstances(List<SPSubstance> substances) {
		this.substances = substances;
	}

	public List<SPCalculatedParameter> getCalculatedParameters() {
		return calculatedParameters;
	}

	public void setCalculatedParameters(
			List<SPCalculatedParameter> calculatedParameters) {
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
