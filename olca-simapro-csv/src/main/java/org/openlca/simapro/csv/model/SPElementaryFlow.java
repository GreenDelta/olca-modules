package org.openlca.simapro.csv.model;

import org.openlca.simapro.csv.model.types.ElementaryFlowType;
import org.openlca.simapro.csv.model.types.SubCompartment;

/**
 * This class represents an elementary flow in SimaPro
 */
public class SPElementaryFlow extends SPFlow {

	/**
	 * The distribution of the flow
	 */
	private IDistribution distribution;

	/**
	 * The sub compartment of the flow
	 */
	private SubCompartment subCompartment;

	/**
	 * The type of elementary flow
	 */
	private ElementaryFlowType type;

	/**
	 * The substance of the flow
	 */
	private SPSubstance substance;

	/**
	 * Creates a new elementary flow
	 * 
	 * @param type
	 *            The type of the flow
	 * @param substance
	 *            The substance of the flow
	 * @param unit
	 *            The unit of the flow
	 * @param amount
	 *            The amount of the flow
	 */
	public SPElementaryFlow(ElementaryFlowType type, SPSubstance substance,
			SPUnit unit, String amount) {
		super(amount, unit);
		this.type = type;
		this.substance = substance;
	}

	/**
	 * Creates a new elementary flow
	 * 
	 * @param type
	 *            The type of the flow
	 * @param subCompartment
	 *            The sub compartment of the flow
	 * @param substance
	 *            The substance of the flow
	 * @param unit
	 *            The unit of the flow
	 * @param amount
	 *            The amount of the flow
	 */
	public SPElementaryFlow(ElementaryFlowType type,
			SubCompartment subCompartment, SPSubstance substance, SPUnit unit,
			String amount) {
		super(amount, unit);
		this.type = type;
		this.subCompartment = subCompartment;
		this.substance = substance;
	}

	/**
	 * Creates a new elementary flow
	 * 
	 * @param type
	 *            The type of the flow
	 * @param subCompartment
	 *            The sub compartment of the flow
	 * @param substance
	 *            The substance of the flow
	 * @param unit
	 *            The unit of the flow
	 * @param amount
	 *            The amount of the flow
	 * @param comment
	 *            A comment to the flow
	 * @param distribution
	 *            The distribution of the flow
	 */
	public SPElementaryFlow(ElementaryFlowType type,
			SubCompartment subCompartment, SPSubstance substance, SPUnit unit,
			String amount, String comment, IDistribution distribution) {
		super(amount, unit, comment);
		this.type = type;
		this.subCompartment = subCompartment;
		this.substance = substance;
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
	 * Getter of the sub compartment
	 * 
	 * @see SubCompartment
	 * @return The sub compartment of the flow
	 */
	public SubCompartment getSubCompartment() {
		return subCompartment;
	}

	/**
	 * Getter of the type of the flow
	 * 
	 * @see ElementaryFlowType
	 * @return The type of the flow
	 */
	public ElementaryFlowType getType() {
		return type;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The substance behind the flow
	 */
	public SPSubstance getSubstance() {
		return substance;
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
	 * Setter of the substance
	 * 
	 * @param substance
	 *            The new substance
	 */
	public void setSubstance(SPSubstance substance) {
		this.substance = substance;
	}

	/**
	 * Setter of the sub compartment
	 * 
	 * @param subCompartment
	 *            The new sub compartment
	 */
	public void setSubCompartment(SubCompartment subCompartment) {
		this.subCompartment = subCompartment;
	}

	/**
	 * Setter of the type
	 * 
	 * @param type
	 *            The new type
	 */
	public void setType(ElementaryFlowType type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return substance.getName();
	}

	@Override
	public void setName(String name) {
		substance.setName(name);
	}

}
