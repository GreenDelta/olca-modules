package org.openlca.simapro.csv.model;

/**
 * This class represents the main output of a process
 */
public class SPProduct extends SPFlow {

	private double allocation = 100;
	private String wasteType;
	private String category;

	public SPProduct(String name, String unit, String amount) {
		super(amount, unit);
		this.name = name;
	}

	public SPProduct(String name, String unit, String amount,
			double allocation, String wasteType, String comment, String category) {
		super(amount, unit, comment);
		this.name = name;
		this.allocation = allocation;
		this.wasteType = wasteType;
		this.category = category;
	}

	public double getAllocation() {
		return allocation;
	}

	public String getWasteType() {
		return wasteType;
	}

	public String getCategory() {
		return category;
	}

	public void setAllocation(double allocation) {
		this.allocation = allocation;
	}

	public void setWasteType(String wasteType) {
		this.wasteType = wasteType;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
