package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_parameters")
public class Parameter extends AbstractEntity {

	@Column(name = "name")
	private String name;

	@Lob
	@Column(name = "description")
	private String description;

	@Column(name = "scope")
	@Enumerated(EnumType.STRING)
	private ParameterScope scope = ParameterScope.GLOBAL;

	@Column(name = "is_input_param")
	private boolean inputParameter;

	@Column(name = "value")
	private double value;

	@Column(name = "formula")
	private String formula;

	@Embedded
	private Uncertainty uncertainty;

	@Column(name = "external_source")
	private String externalSource;

	@Column(name = "source_type")
	private String sourceType;

	/**
	 * Returns true if the given name is a valid identifier for a parameter. We
	 * allow the same rules as for Java identifiers.
	 */
	public static boolean isValidName(String paramaterName) {
		if (paramaterName == null)
			return false;
		String id = paramaterName.trim();
		if (id.isEmpty())
			return false;
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (i == 0 && !Character.isLetter(c))
				return false;
			if (i > 0 && !Character.isJavaIdentifierPart(c))
				return false;
		}
		return true;
	}

	@Override
	public Parameter clone() {
		Parameter clone = new Parameter();
		clone.setDescription(getDescription());
		clone.setFormula(getFormula());
		clone.setInputParameter(isInputParameter());
		clone.setName(getName());
		clone.setScope(getScope());
		if (getUncertainty() != null)
			clone.setUncertainty(getUncertainty().clone());
		clone.setValue(getValue());
		clone.setExternalSource(getExternalSource());
		clone.setSourceType(getSourceType());
		return clone;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public void setScope(ParameterScope type) {
		this.scope = type;
	}

	public void setDescription(String description) {
		this.description = description;
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

	public ParameterScope getScope() {
		return scope;
	}

	public Uncertainty getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(Uncertainty uncertainty) {
		this.uncertainty = uncertainty;
	}

	public String getExternalSource() {
		return externalSource;
	}

	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public String toString() {
		return "Parameter [formula=" + formula + ", name=" + name + ", type="
				+ scope + "]";
	}

}
