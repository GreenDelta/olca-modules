package org.openlca.ilcd.productmodel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.FIELD)
public class Parameter {

	@XmlAttribute
	protected String name;

	@XmlAttribute
	protected String formula;

	@XmlAttribute(required = false)
	protected ParameterScopeValues scope;

	@XmlAttribute
	protected Double value;

	public Parameter() {
	};

	public Parameter(String name, String formula) {
		this.name = name;
		this.formula = formula;
	}

	public Parameter(String name, String formula, ParameterScopeValues scope) {
		this.name = name;
		this.formula = formula;
		this.scope = scope;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the formula
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * @param formula
	 *            the formula to set
	 */
	public void setFormula(String formula) {
		this.formula = formula;
	}

	/**
	 * @return the scope
	 */
	public ParameterScopeValues getScope() {
		return scope;
	}

	/**
	 * @param scope
	 *            the scope to set
	 */
	public void setScope(ParameterScopeValues scope) {
		this.scope = scope;
	}

	public Double getValue() {
		return value;
	}

	public void setValue(Double value) {
		this.value = value;
	}

}
