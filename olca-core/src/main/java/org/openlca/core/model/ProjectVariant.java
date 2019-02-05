package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_project_variants")
public class ProjectVariant extends AbstractEntity {

	@Column(name = "name")
	public String name;

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
	public ProjectVariant clone() {
		ProjectVariant clone = new ProjectVariant();
		clone.name = name;
		clone.productSystem = productSystem;
		clone.unit = unit;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.amount = amount;
		clone.allocationMethod = allocationMethod;
		for (ParameterRedef redef : parameterRedefs) {
			clone.parameterRedefs.add(redef.clone());
		}
		clone.isDisabled = isDisabled;
		return clone;
	}
}
