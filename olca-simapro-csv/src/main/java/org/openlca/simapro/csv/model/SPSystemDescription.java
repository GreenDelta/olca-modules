package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro system description
 */
public class SPSystemDescription {

	/**
	 * The allocation rules of the system
	 */
	private String allocationRules;

	/**
	 * The category of the system description
	 */
	private String category;

	/**
	 * The cut off rules of the system
	 */
	private String cutOffRules;

	/**
	 * The description of the system
	 */
	private String description;

	/**
	 * The energy model of the system
	 */
	private String energyModel;

	/**
	 * The name of the system
	 */
	private String name;

	/**
	 * Other assumptions of the system
	 */
	private String otherAssumptions;

	/**
	 * Other information of the system
	 */
	private String otherInformation;

	/**
	 * Sub systems of the system
	 */
	private String subSystems;

	/**
	 * The transport model of the system
	 */
	private String transportModel;

	/**
	 * The waste model of the system
	 */
	private String wasteModel;

	/**
	 * Creates a new system description
	 * 
	 * @param name
	 *            The name of the system
	 * @param description
	 *            The description of the system
	 */
	public SPSystemDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}

	/**
	 * Creates a new system description
	 * 
	 * @param name
	 *            The name of the system
	 * @param description
	 *            The description of the system
	 * @param category
	 *            The category of the system description
	 */
	public SPSystemDescription(String name, String description, String category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	/**
	 * Getter of the allocation rules
	 * 
	 * @return The allocation rules of the described system
	 */
	public String getAllocationRules() {
		return allocationRules;
	}

	/**
	 * Getter of the category
	 * 
	 * @return The category of the system description
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Getter of the cut off rules
	 * 
	 * @return The the cut off rules of the described system
	 */
	public String getCutOffRules() {
		return cutOffRules;
	}


	/**
	 * Getter of the description
	 * 
	 * @return The system description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Getter of the energy model
	 * 
	 * @return The energy model of the described system
	 */
	public String getEnergyModel() {
		return energyModel;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the described system
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter of the other assumptions
	 * 
	 * @return The other assumptions of the described system
	 */
	public String getOtherAssumptions() {
		return otherAssumptions;
	}

	/**
	 * Getter of the other information
	 * 
	 * @return The other information of the described system
	 */
	public String getOtherInformation() {
		return otherInformation;
	}

	/**
	 * Getter of the sub systems
	 * 
	 * @return The sub systems of the described system
	 */
	public String getSubSystems() {
		return subSystems;
	}

	/**
	 * Getter of the transport model
	 * 
	 * @return The transport model of the described system
	 */
	public String getTransportModel() {
		return transportModel;
	}

	/**
	 * Getter of the waste model
	 * 
	 * @return The waste model of the described system
	 */
	public String getWasteModel() {
		return wasteModel;
	}

	/**
	 * Setter of the allocation rules
	 * 
	 * @param allocationRules
	 *            The new allocation rules
	 */
	public void setAllocationRules(String allocationRules) {
		this.allocationRules = allocationRules;
	}

	/**
	 * Setter of the category
	 * 
	 * @param category
	 *            The new category
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * Setter of the cut off rules
	 * 
	 * @param cutOffRules
	 *            The new cut off rules
	 */
	public void setCutOffRules(String cutOffRules) {
		this.cutOffRules = cutOffRules;
	}

	/**
	 * Setter of the description
	 * 
	 * @param description
	 *            The new description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Setter of the energy model
	 * 
	 * @param energyModel
	 *            The new energy model
	 */
	public void setEnergyModel(String energyModel) {
		this.energyModel = energyModel;
	}

	/**
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of other assumptions
	 * 
	 * @param otherAssumptions
	 *            The new other assumptions
	 */
	public void setOtherAssumptions(String otherAssumptions) {
		this.otherAssumptions = otherAssumptions;
	}

	/**
	 * Setter of other information
	 * 
	 * @param otherInformation
	 *            The new other information
	 */
	public void setOtherInformation(String otherInformation) {
		this.otherInformation = otherInformation;
	}

	/**
	 * Setter of the sub systems
	 * 
	 * @param subSystems
	 *            The new sub systems
	 */
	public void setSubSystems(String subSystems) {
		this.subSystems = subSystems;
	}

	/**
	 * Setter of the transport model
	 * 
	 * @param transportModel
	 *            The new transport model
	 */
	public void setTransportModel(String transportModel) {
		this.transportModel = transportModel;
	}

	/**
	 * Setter of the waste model
	 * 
	 * @param wasteModel
	 *            The new waste model
	 */
	public void setWasteModel(String wasteModel) {
		this.wasteModel = wasteModel;
	}

}
