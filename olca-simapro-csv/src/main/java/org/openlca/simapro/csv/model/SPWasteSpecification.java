package org.openlca.simapro.csv.model;

/**
 * This class represents a waste specification of a waste treatment
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPWasteSpecification extends SPFlow {

	private String wasteType;

	private String category;

	public SPWasteSpecification(String name, String unit, String amount) {
		super(amount, unit);
		this.name = name;
	}

	public SPWasteSpecification(String name, String unit, String amount,
			String wasteType, String comment, String category) {
		super(amount, unit, comment);
		this.name = name;
		this.wasteType = wasteType;
		this.category = category;
	}

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
