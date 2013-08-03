package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.ProductFlowType;

/**
 * This class represents a product input in SimaPro
 */
public class SPProductFlow extends SPFlow {

	/**
	 * The distribution of the flow
	 */
	private IDistribution distribution;

	/**
	 * The type of product flow
	 */
	private ProductFlowType type;

	/**
	 * The name of the flow
	 */
	private String name;

	/**
	 * Creates a new product flow
	 * 
	 * @param type
	 *            The type of the flow
	 * @param name
	 *            The name of the flow
	 * @param unit
	 *            The unit of the flow
	 * @param amount
	 *            The amount of the flow
	 */
	public SPProductFlow(ProductFlowType type, String name, SPUnit unit,
			String amount) {
		super(amount, unit);
		this.type = type;
		this.name = name;
	}

	/**
	 * Creates a new product flow
	 * 
	 * @param type
	 *            The type of the flow
	 * @param name
	 *            The name of the flow
	 * @param unit
	 *            The unit of the flow
	 * @param amount
	 *            The amount of the flow
	 * @param comment
	 *            A comment to the flow
	 * @param distribution
	 *            The distribution of the flow
	 */
	public SPProductFlow(ProductFlowType type, String name, SPUnit unit,
			String amount, String comment, IDistribution distribution) {
		super(amount, unit, comment);
		this.type = type;
		this.name = name;
		this.distribution = distribution;
	}

	/**
	 * Getter of the distribution
	 * 
	 * @see IDistribution
	 * @return The uncertainty distribution of the flow
	 */
	public IDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Getter of the type of the flow
	 * 
	 * @see ProductFlowType
	 * @return The type of the flow
	 */
	public ProductFlowType getType() {
		return type;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the flow
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Setter of the distribution
	 * 
	 * @param distribution
	 *            The new distribution
	 */
	public void setDistribution(IDistribution distribution) {
		this.distribution = distribution;
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
	 * Setter of the type
	 * 
	 * @param type
	 *            The new type
	 */
	public void setType(ProductFlowType type) {
		this.type = type;
	}

}
