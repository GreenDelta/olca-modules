package org.openlca.simapro.csv.model;

/**
 * This class represents the main output of a process
 */
public class SPReferenceProduct extends SPFlow {

	/**
	 * The name of the product
	 */
	private String name;

	/**
	 * The allocation of the product
	 */
	private double allocation = 100;

	/**
	 * The waste type of the product
	 */
	private String wasteType;

	private String category;

	/**
	 * Creates a new reference product
	 * 
	 * @param name
	 *            The name of the product
	 * @param unit
	 *            The unit of the product
	 * @param amount
	 *            The amount of the product
	 */
	public SPReferenceProduct(String name, SPUnit unit, String amount) {
		super(amount, unit);
		this.name = name;
	}

	/**
	 * Creates a new reference product
	 * 
	 * @param name
	 *            The name of the product
	 * @param unit
	 *            The unit of the product
	 * @param amount
	 *            The amount of the product
	 * @param allocation
	 *            The allocation factor of the product
	 * @param wasteType
	 *            The waste type of the product
	 * @param comment
	 *            A comment to the product
	 */
	public SPReferenceProduct(String name, SPUnit unit, String amount,
			double allocation, String wasteType, String comment, String category) {
		super(amount, unit, comment);
		this.name = name;
		this.allocation = allocation;
		this.wasteType = wasteType;
		this.category = category;
	}

	/**
	 * Getter of the allocation
	 * 
	 * @return The allocation factor of the product
	 */
	public double getAllocation() {
		return allocation;
	}

	/**
	 * Getter of the waste type
	 * 
	 * @return The waste type of the product
	 */
	public String getWasteType() {
		return wasteType;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the product
	 */
	@Override
	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
	}

	/**
	 * Setter of the allocation
	 * 
	 * @param allocation
	 *            The new allocation
	 */
	public void setAllocation(double allocation) {
		this.allocation = allocation;
	}

	/**
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of the waste type
	 * 
	 * @param wasteType
	 *            The new waste type
	 */
	public void setWasteType(String wasteType) {
		this.wasteType = wasteType;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}

}
