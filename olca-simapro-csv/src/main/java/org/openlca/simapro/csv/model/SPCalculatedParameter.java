package org.openlca.simapro.csv.model;

/**
 * This class represents a process parameter with expression (can be a double
 * value or a formula)
 */
public class SPCalculatedParameter {

	/**
	 * The name of the parameter
	 */
	private String name;

	/**
	 * A comment to the parameter
	 */
	private String comment;

	/**
	 * The expression of the parameter
	 */
	private String expression;

	/**
	 * Creates a new calculated parameter
	 * 
	 * @param name
	 *            The name of the parameter
	 * @param expression
	 *            The expression of the parameter
	 */
	public SPCalculatedParameter(String name, String expression) {
		this.name = name;
		this.expression = expression;
	}

	/**
	 * Creates a new calculated parameter
	 * 
	 * @param name
	 *            The name of the parameter
	 * @param expression
	 *            The expression of the parameter
	 * @param comment
	 *            A comment to the parameter
	 */
	public SPCalculatedParameter(String name, String expression, String comment) {
		this.name = name;
		this.comment = comment;
		this.expression = expression;
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
	 * Getter of the expression
	 * 
	 * @return The expression of the parameter (can be a double value or a
	 *         formula)
	 */
	public String getExpression() {
		return expression;
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
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name of the parameter
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of the comment
	 * 
	 * @param comment
	 *            The new comment to the parameter
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * Setter of the expression
	 * 
	 * @param expression
	 *            The new expression of the parameter
	 */
	public void setExpression(String expression) {
		this.expression = expression;
	}

}
