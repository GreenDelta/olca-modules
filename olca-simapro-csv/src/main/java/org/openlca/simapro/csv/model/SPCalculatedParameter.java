package org.openlca.simapro.csv.model;

/**
 * This class represents a process parameter with expression (can be a double
 * value or a formula)
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPCalculatedParameter extends SPParameter {

	private String expression;

	public SPCalculatedParameter(String name, String expression) {
		this.name = name;
		this.expression = expression;
	}

	public SPCalculatedParameter(String name, String expression, String comment) {
		this.name = name;
		this.comment = comment;
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

}
