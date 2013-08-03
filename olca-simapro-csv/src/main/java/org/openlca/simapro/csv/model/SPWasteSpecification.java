package org.openlca.simapro.csv.model;

/**
 * This class represents a waste specification of a waste treatment
 */
public class SPWasteSpecification extends SPFlow {

	/**
	 * The waste type of the waste specification
	 */
	private String wasteType;

	/**
	 * The name of the waste specification
	 */
	private String name;

	private String category;


	/**
	 * Creates a new waste specification
	 * 
	 * @param name
	 *            The name of the waste specification
	 * @param unit
	 *            The unit of the waste specification
	 * @param amount
	 *            The amount of the waste specification
	 */
	public SPWasteSpecification(String name, SPUnit unit, String amount) {
		super(amount, unit);
		this.name = name;
	}

	/**
	 * Creates a new product flow
	 * 
	 * @param name
	 *            The name of the waste specification
	 * @param unit
	 *            The unit of the waste specification
	 * @param amount
	 *            The amount of the waste specification
	 * @param wasteType
	 *            The waste type of the waste specification
	 * @param comment
	 *            A comment to the waste specification
	 */
	public SPWasteSpecification(String name, SPUnit unit, String amount,
			String wasteType, String comment, String category) {
		super(amount, unit, comment);
		this.name = name;
		this.wasteType = wasteType;
		this.category = category;
	}

	/**
	 * Getter of the waste type
	 * 
	 * @return The waste type of the waste specification
	 */
	public String getWasteType() {
		return wasteType;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the waste specification
	 */
	@Override
	public String getName() {
		return name;
	}
	
	public String getCategory() {
		return category;
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
