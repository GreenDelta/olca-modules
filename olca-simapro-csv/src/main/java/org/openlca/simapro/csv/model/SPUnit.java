package org.openlca.simapro.csv.model;

/**
 * This class represents a SimaPro unit
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPUnit {

	/**
	 * The conversion factor to the reference unit of the parent quantity
	 */
	private double conversionFactor = 1;

	/**
	 * The name of the unit
	 */
	private String name;

	private String referenceUnit;

	private String quantity;

	/**
	 * Creates a new unit
	 * 
	 * @param name
	 *            The name of the unit
	 */
	public SPUnit(String name) {
		this.name = name;
	}

	/**
	 * Creates a new unit
	 * 
	 * @param name
	 *            The name of the unit
	 * @param conversionFactor
	 *            The conversion factor of the unit
	 */
	public SPUnit(String name, double conversionFactor) {
		this.name = name;
		this.conversionFactor = conversionFactor;
	}

	/**
	 * Getter of the conversion factor
	 * 
	 * @return The conversion factor to the reference unit of the quantity the
	 *         unit is in
	 */
	public double getConversionFactor() {
		return conversionFactor;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the unit
	 */
	public String getName() {
		return name;
	}

	/**
	 * Setter of the conversion factor
	 * 
	 * @param conversionFactor
	 *            The new conversion factor
	 */
	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
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

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	/**
	 * The name of the reference unit
	 * 
	 * @return
	 */
	public String getReferenceUnit() {
		return referenceUnit;
	}

	/**
	 * The name of the reference unit
	 * 
	 * @param unit
	 */
	public void setReferenceUnit(String unit) {
		this.referenceUnit = unit;
	}

}
