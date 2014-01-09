package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro system description
 */
public class SPSystemDescription {

	private String allocationRules;
	private String category;
	private String cutOffRules;
	private String description;
	private String energyModel;
	private String name;
	private String otherAssumptions;
	private String otherInformation;
	private String subSystems;
	private String transportModel;
	private String wasteModel;

	public SPSystemDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public SPSystemDescription(String name, String description, String category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	public String getAllocationRules() {
		return allocationRules;
	}

	public String getCategory() {
		return category;
	}

	public String getCutOffRules() {
		return cutOffRules;
	}

	public String getDescription() {
		return description;
	}

	public String getEnergyModel() {
		return energyModel;
	}

	public String getName() {
		return name;
	}

	public String getOtherAssumptions() {
		return otherAssumptions;
	}

	public String getOtherInformation() {
		return otherInformation;
	}

	public String getSubSystems() {
		return subSystems;
	}

	public String getTransportModel() {
		return transportModel;
	}

	public String getWasteModel() {
		return wasteModel;
	}

	public void setAllocationRules(String allocationRules) {
		this.allocationRules = allocationRules;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setCutOffRules(String cutOffRules) {
		this.cutOffRules = cutOffRules;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEnergyModel(String energyModel) {
		this.energyModel = energyModel;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setOtherAssumptions(String otherAssumptions) {
		this.otherAssumptions = otherAssumptions;
	}

	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
	}

	public void setSubSystems(String subSystems) {
		this.subSystems = subSystems;
	}

	public void setTransportModel(String transportModel) {
		this.transportModel = transportModel;
	}

	public void setWasteModel(String wasteModel) {
		this.wasteModel = wasteModel;
	}

}
