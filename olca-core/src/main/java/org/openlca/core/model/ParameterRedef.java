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

	/**
	 * The name of the parameter that should be redefined.
	 */
	@Column(name = "name")
	public String name;

	@Column(name = "description")
	public String description;

	/**
	 * The ID of the process or LCIA method for which the redefinition is valid.
	 * If the context ID is null it is a redefinition of a global parameter.
	 */
	@Column(name = "f_context")
	public Long contextId;

	/**
	 * The type of the context where the original parameter is defined
	 * (currently only processes or LCIA methods are possible). For global
	 * parameter redefinitions the context type is null.
	 */
	@Column(name = "context_type")
	@Enumerated(EnumType.STRING)
	public ModelType contextType;

	@Column(name = "value")
	public double value;

	@Embedded
	public Uncertainty uncertainty;

	@Override
	public ParameterRedef clone() {
		ParameterRedef clone = new ParameterRedef();
		clone.name = name;
		clone.description = description;
		clone.contextId = contextId;
		clone.contextType = contextType;
		clone.value = value;
		if (uncertainty != null) {
			clone.uncertainty = uncertainty.clone();
		}
		return clone;
	}
}
