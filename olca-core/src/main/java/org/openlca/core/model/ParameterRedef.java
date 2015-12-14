package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

/**
 * A redefinition of a parameter in a project or product systems. The
 * redefinition defines a context for the redefinition which is the process or
 * LCIA method for which the redefinition is valid. If there is no such context
 * given it is a redefinition of a global parameter.
 */
@Entity
@Table(name = "tbl_parameter_redefs")
public class ParameterRedef extends AbstractEntity {

	@Column(name = "name")
	private String name;

	@Column(name = "f_context")
	private Long contextId;

	@Column(name = "context_type")
	@Enumerated(EnumType.STRING)
	private ModelType contextType;

	@Column(name = "value")
	private double value;

	@Embedded
	private Uncertainty uncertainty;

	@Override
	public ParameterRedef clone() {
		ParameterRedef clone = new ParameterRedef();
		clone.setName(getName());
		clone.setContextId(getContextId());
		clone.setContextType(getContextType());
		clone.setValue(getValue());
		if (getUncertainty() != null)
			clone.setUncertainty(getUncertainty().clone());
		return clone;
	}

	/**
	 * Get the name of the parameter that should be redefined.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the parameter that should be redefined.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get the ID of the process or LCIA method for which the redefinition is
	 * valid. If the context ID is null it is a redefinition of a global
	 * parameter.
	 */
	public Long getContextId() {
		return contextId;
	}

	/**
	 * Set the ID of the process or LCIA method for which the redefinition is
	 * valid. If the context ID is null it is a redefinition of a global
	 * parameter.
	 */
	public void setContextId(Long contextId) {
		this.contextId = contextId;
	}

	/**
	 * Returns the type of the context where the original parameter is defined
	 * (currently only processes or LCIA methods are possible). For global
	 * parameter redefinitions the context type is null.
	 */
	public ModelType getContextType() {
		return contextType;
	}

	/**
	 * Set the type of the context where the original parameter is defined
	 * (currently only processes or LCIA methods are possible). For global
	 * parameter redefinitions the context type is null.
	 */
	public void setContextType(ModelType contextType) {
		this.contextType = contextType;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public Uncertainty getUncertainty() {
		return uncertainty;
	}

	public void setUncertainty(Uncertainty uncertainty) {
		this.uncertainty = uncertainty;
	}

}
