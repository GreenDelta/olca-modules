package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_impact_factors")
public class ImpactFactor extends AbstractEntity
	implements Copyable<ImpactFactor> {

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

	@OneToOne
	@JoinColumn(name = "f_location")
	public Location location;

	public static ImpactFactor of(Flow flow, double value) {
		var f = new ImpactFactor();
		f.value = value;
		if (flow != null) {
			f.flow = flow;
			f.flowPropertyFactor = flow.getReferenceFactor();
			f.unit = flow.getReferenceUnit();
		}
		return f;
	}

	@Override
	public ImpactFactor copy() {
		var clone = new ImpactFactor();
		clone.flow = flow;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.unit = unit;
		clone.value = value;
		clone.formula = formula;
		if (uncertainty != null) {
			clone.uncertainty = uncertainty.copy();
		}
		clone.location = location;
		return clone;
	}

}
