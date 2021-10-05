package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_project_variants")
public class ProjectVariant extends AbstractEntity
	implements Copyable<ProjectVariant> {

	@Column(name = "name")
	public String name;

	@Column(name = "description")
	public String description;

	@OneToOne
	@JoinColumn(name = "f_product_system")
	public ProductSystem productSystem;

	@OneToOne
	@JoinColumn(name = "f_unit")
	public Unit unit;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	public FlowPropertyFactor flowPropertyFactor;

	@Column(name = "amount")
	public double amount;

	@Column(name = "allocation_method")
	@Enumerated(EnumType.STRING)
	public AllocationMethod allocationMethod;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	public final List<ParameterRedef> parameterRedefs = new ArrayList<>();

	/**
	 * Indicates that this variant is disabled. When it is disabled it is not
	 * considered in the result calculation.
	 */
	@Column(name = "is_disabled")
	public boolean isDisabled;

	@Override
	public ProjectVariant copy() {
		var clone = new ProjectVariant();
		clone.name = name;
		clone.description = description;
		clone.productSystem = productSystem;
		clone.unit = unit;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.amount = amount;
		clone.allocationMethod = allocationMethod;
		for (var redef : parameterRedefs) {
			clone.parameterRedefs.add(redef.copy());
		}
		clone.isDisabled = isDisabled;
		return clone;
	}
}
