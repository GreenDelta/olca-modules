package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * A redefinition of a parameter in a project or product systems. The
 * redefinition defines a context for the redefinition which is the process or
 * LCIA method for which the redefinition is valid. If there is no such context
 * given it is a redefinition of a global parameter.
 */
@Entity
@Table(name = "tbl_parameter_redefs")
public class ParameterRedef extends AbstractEntity
	implements Copyable<ParameterRedef> {

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

	/**
	 * Indicates that this parameter redefinition is protected. A protected
	 * redefinition cannot be overwritten by in a calculation.
	 */
	@Column(name = "is_protected")
	public boolean isProtected;

	public static ParameterRedef of(Parameter param, double value) {
		return of(param, null, value);
	}

	public static ParameterRedef of(Parameter param) {
		return of(param, null);
	}

	public static ParameterRedef of(
		Parameter param, ParameterizedEntity owner, double value) {
		var p = of(param, owner);
		p.value = value;
		return p;
	}

	public static ParameterRedef of(Parameter param, ParameterizedEntity owner) {
		var redef = new ParameterRedef();
		if (param != null) {
			redef.name = param.name;
			redef.value = param.value;
			redef.uncertainty = param.uncertainty;
			redef.description = param.description;
		}
		if (owner != null) {
			redef.contextId = owner.id;
			redef.contextType = ModelType.forModelClass(owner.getClass());
		}
		return redef;
	}

	public static ParameterRedef of(String name) {
		var redef = new ParameterRedef();
		redef.name = name;
		redef.value = 1.0;
		return redef;
	}

	@Override
	public ParameterRedef copy() {
		var clone = new ParameterRedef();
		clone.name = name;
		clone.description = description;
		clone.contextId = contextId;
		clone.contextType = contextType;
		clone.value = value;
		clone.isProtected = isProtected;
		if (uncertainty != null) {
			clone.uncertainty = uncertainty.copy();
		}
		return clone;
	}
}
