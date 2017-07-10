package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_impact_factors")
public class ImpactFactor extends AbstractEntity implements Cloneable {

	@OneToOne
	@JoinColumn(name = "f_flow")
	public Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	public FlowPropertyFactor flowPropertyFactor;

	@OneToOne
	@JoinColumn(name = "f_unit")
	public Unit unit;

	@Column(name = "value")
	public double value = 1;

	@Column(name = "formula")
	public String formula;

	@Embedded
	public Uncertainty uncertainty;

	@Override
	public ImpactFactor clone() {
		ImpactFactor clone = new ImpactFactor();
		clone.flow = flow;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.unit = unit;
		clone.value = value;
		clone.formula = formula;
		if (uncertainty != null)
			clone.uncertainty = uncertainty.clone();
		return clone;
	}

}
