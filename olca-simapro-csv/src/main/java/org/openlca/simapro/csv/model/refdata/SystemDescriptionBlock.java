package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.SectionValue;

@BlockModel("System description")
public class SystemDescriptionBlock {

	@SectionValue("Name")
	private String name;

	@SectionValue("Category")
	private String category;

	@SectionValue("Description")
	private String description;

	@SectionValue("Sub-systems")
	private String subSystems;

	@SectionValue("Cut-off rules")
	private String cutOffRules;

	@SectionValue("Energy model")
	private String energyModel;

	@SectionValue("Transport model")
	private String transportModel;

	@SectionValue("Waste model")
	private String wasteModel;

	@SectionValue("Other assumptions")
	private String otherAssumptions;

	@SectionValue("Other information")
	private String otherInformation;

	@SectionValue("Allocation rules")
	private String allocationRules;

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
