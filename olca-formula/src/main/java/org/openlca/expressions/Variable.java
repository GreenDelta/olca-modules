package org.openlca.expressions;

public class Variable {

	private String name;

	private String expression;

	private Double value;

	public Variable() {
	}

	public Variable(String name, String expression) {
		this.name = name;
		this.expression = expression;

	}

	public Variable(String name, Double value) {
		this.name = name;
		this.value = value;
	}

	public Variable(String name, String expression, Double value) {
		this.name = name;
		this.expression = expression;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

	public String getExpression() {
		String exp = expression;
		if (exp == null && value != null)
			exp = value.toString();
		return exp;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public boolean isEvaluated() {
		return value != null;
	}
}
