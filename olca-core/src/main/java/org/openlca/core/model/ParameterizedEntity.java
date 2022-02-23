package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

/**
 * A parameterized entity is a model type that can contain parameters. The only
 * parameterized entities that we currently have in our model are processes and
 * impact categories. In the formula evaluation, parameterized entities have
 * their own scope.
 */
@MappedSuperclass
public abstract class ParameterizedEntity extends RootEntity {

	/**
	 * The parameters that are owned by this model and that are only valid in the
	 * scope of that model.
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<Parameter> parameters = new ArrayList<>();

	/**
	 * Adds an input parameter with the given name and value to this process.
	 */
	public Parameter parameter(String name, double value) {
		var param = new Parameter();
		param.name = name;
		param.refId = UUID.randomUUID().toString();
		param.scope = parameterScope();
		param.isInputParameter = true;
		param.value = value;
		parameters.add(param);
		return param;
	}

	/**
	 * Adds a calculated parameter with the given name and formula to this
	 * process.
	 */
	public Parameter parameter(String name, String formula) {
		var param = new Parameter();
		param.name = name;
		param.refId = UUID.randomUUID().toString();
		param.scope = parameterScope();
		param.isInputParameter = false;
		param.formula = formula;
		parameters.add(param);
		return param;
	}

	/**
	 * Get the type of the parameter scope of this entity.
	 */
	public abstract ParameterScope parameterScope();
}
