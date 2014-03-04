package org.openlca.simapro.csv.model;

public class SPProduct extends SPExchange {

	private double allocation = 100;
	private String wasteType;
	private String category;

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
