package org.openlca.core.matrix;

import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.UncertaintyType;

class CalcParameter {

	private String name;
	private boolean inputParameter;
	private long owner;
	private ParameterScope scope;
	private double value;
	private String formula;

	private UncertaintyType uncertaintyType;
	private double parameter1;
	private double parameter2;
	private double parameter3;
	private String parameter1Formula;
	private String parameter2Formula;
	private String parameter3Formula;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isInputParameter() {
		return inputParameter;
	}

	public void setInputParameter(boolean inputParameter) {
		this.inputParameter = inputParameter;
	}

	public long getOwner() {
		return owner;
	}

	public void setOwner(long owner) {
		this.owner = owner;
	}

	public ParameterScope getScope() {
		return scope;
	}

	public void setScope(ParameterScope scope) {
		this.scope = scope;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String getFormula() {
		return formula;
	}

	public void setFormula(String formula) {
		this.formula = formula;
	}

	public UncertaintyType getUncertaintyType() {
		return uncertaintyType;
	}

	public void setUncertaintyType(UncertaintyType uncertaintyType) {
		this.uncertaintyType = uncertaintyType;
	}

	public double getParameter1() {
		return parameter1;
	}

	public void setParameter1(double parameter1) {
		this.parameter1 = parameter1;
	}

	public double getParameter2() {
		return parameter2;
	}

	public void setParameter2(double parameter2) {
		this.parameter2 = parameter2;
	}

	public double getParameter3() {
		return parameter3;
	}

	public void setParameter3(double parameter3) {
		this.parameter3 = parameter3;
	}

	public String getParameter1Formula() {
		return parameter1Formula;
	}

	public void setParameter1Formula(String parameter1Formula) {
		this.parameter1Formula = parameter1Formula;
	}

	public String getParameter2Formula() {
		return parameter2Formula;
	}

	public void setParameter2Formula(String parameter2Formula) {
		this.parameter2Formula = parameter2Formula;
	}

	public String getParameter3Formula() {
		return parameter3Formula;
	}

	public void setParameter3Formula(String parameter3Formula) {
		this.parameter3Formula = parameter3Formula;
	}

}
