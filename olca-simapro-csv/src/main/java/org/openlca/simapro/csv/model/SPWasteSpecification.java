package org.openlca.simapro.csv.model;

/**
 * This class represents a waste specification of a waste treatment
 */
public class SPWasteSpecification extends SPExchange {

	private String wasteType;

	private String category;

	public String getWasteType() {
		return wasteType;
	}

	public String getCategory() {
		return category;
	}

	public void setWasteType(String wasteType) {
		this.wasteType = wasteType;
	}

	public void setCategory(String category) {
		this.category = category;
	}
}
