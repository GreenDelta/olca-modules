package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToMany;

/**
 * A parameterized entity is a model type that can contain parameters. The only
 * parameterized entities that we currently have in our model are processes and
 * impact categories. In the formula evaluation, parameterized entities have
 * their own scope.
 */
@MappedSuperclass
public abstract class ParameterizedEntity extends CategorizedEntity {

	/**
	 * The parameters that are owned by this model and that are only valid in the
	 * scope of that model.
	 */
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<Parameter> parameters = new ArrayList<>();

}
