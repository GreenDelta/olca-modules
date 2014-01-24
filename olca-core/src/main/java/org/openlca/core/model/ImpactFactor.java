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
	private Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property_factor")
	private FlowPropertyFactor flowPropertyFactor;

	@OneToOne
	@JoinColumn(name = "f_unit")
	private Unit unit;

	@Column(name = "value")
	private double value = 1;

	@Embedded
	private Uncertainty uncertainty;

	@Override
	public ImpactFactor clone() {
		ImpactFactor clone = new ImpactFactor();
		clone.setFlow(getFlow());
		clone.setFlowPropertyFactor(getFlowPropertyFactor());
		clone.setUnit(getUnit());
		clone.setValue(getValue());
		if (getUncertainty() != null)
			clone.setUncertainty(getUncertainty().clone());
		return clone;
	}

	public Flow getFlow() {
		return flow;
	}

	public void setFlow(Flow flow) {
		this.flow = flow;
	}

	public FlowPropertyFactor getFlowPropertyFactor() {
		return flowPropertyFactor;
	}

	public void setFlowPropertyFactor(FlowPropertyFactor flowPropertyFactor) {
		this.flowPropertyFactor = flowPropertyFactor;
	}

	public Unit getUnit() {
		return unit;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
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
