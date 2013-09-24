package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * A conversion factor between two quantities of a flow.
 */
@Entity
@Table(name = "tbl_flow_property_factors")
public class FlowPropertyFactor extends AbstractEntity {

	@Column(name = "conversion_factor")
	private double conversionFactor = 1d;

	@OneToOne
	@JoinColumn(name = "f_flow_property")
	private FlowProperty flowProperty;

	@Override
	public FlowPropertyFactor clone() {
		final FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.setConversionFactor(getConversionFactor());
		factor.setFlowProperty(getFlowProperty());
		return factor;
	}

	public double getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(double conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public FlowProperty getFlowProperty() {
		return flowProperty;
	}

	public void setFlowProperty(FlowProperty flowProperty) {
		this.flowProperty = flowProperty;
	}

}
