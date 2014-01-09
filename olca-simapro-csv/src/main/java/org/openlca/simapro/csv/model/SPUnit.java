package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro unit <<<<<<< Updated upstream =======
 * 
 * >>>>>>> Stashed changes
 */
public class SPUnit {

	private double conversionFactor = 1;
	private String name;
	private String referenceUnit;
	private String quantity;

	public SPUnit(String name) {
		this.name = name;
	}

	public SPUnit(String name, double conversionFactor) {
		this.name = name;
		this.conversionFactor = conversionFactor;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public String getName() {
		return name;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getReferenceUnit() {
		return referenceUnit;
	}

	public void setReferenceUnit(String unit) {
		this.referenceUnit = unit;
	}

}
