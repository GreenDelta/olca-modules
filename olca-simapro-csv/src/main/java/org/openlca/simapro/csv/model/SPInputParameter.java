package org.openlca.simapro.csv.model;


/**
 * This class represents a process parameter having a double value (no formula)
 * 
 * @author Sebastian Greve
 * 
 */
public class SPInputParameter {

	/**
	 * A comment to the parameter
	 */
	private String comment;

	/**
	 * The distribution of the parameter
	 */
	private IDistribution distribution;

	/**
	 * The name of the parameter
	 */
	private String name;

	/**
	 * The value of the parameter
	 */
	private double value = 0;

	/**
	 * Indicates if the parameter is hidden or not
	 */
	private boolean hidden = false;

	/**
	 * Creates a new parameter
	 * 
	 * @param name
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 */
	public SPInputParameter(String name, double value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Creates a new parameter
	 * 
	 * @param name
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 * @param distribution
	 *            The distribution of the parameter
	 */
	public SPInputParameter(String name, double value,
			IDistribution distribution) {
		this.name = name;
		this.value = value;
		this.distribution = distribution;
	}

	/**
	 * Creates a new parameter
	 * 
	 * @param name
	 *            The name of the parameter
	 * @param value
	 *            The value of the parameter
	 * @param distribution
	 *            The distribution of the parameter
	 * @param comment
	 *            A comment to the parameter
	 * @param hidden
	 *            Indicates if the parameter is hidden or not
	 */
	public SPInputParameter(String name, double value,
			IDistribution distribution, String comment, boolean hidden) {
		this.name = name;
		this.value = value;
		this.distribution = distribution;
		this.comment = comment;
		this.hidden = hidden;
	}

	/**
	 * Getter of the comment
	 * 
	 * @return An optional comment to the parameter
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Getter of the distribution
	 * 
	 * @see IDistribution
	 * @return The distribution of the parameter
	 */
	public IDistribution getDistribution() {
		return distribution;
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the parameter
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter of the value
	 * 
	 * @return The value of the parameter
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Getter of the hide value
	 * 
	 * @return true if the parameter is hidden, false otherwise
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Setter of the comment
	 * 
	 * @param comment
	 *            The new comment
	 */
	public void setComment(String comment) {
		this.comment = comment;
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
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of the value
	 * 
	 * @param value
	 *            The new value
	 */
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Setter of the hidden value
	 * 
	 * @param hidden
	 *            The new hidden value
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
