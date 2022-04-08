package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * A conversion factor between two quantities of a flow.
 */
@Entity
@Table(name = "tbl_flow_property_factors")
public class FlowPropertyFactor extends AbstractEntity
	implements Copyable<FlowPropertyFactor> {

	@Column(name = "conversion_factor")
	public double conversionFactor = 1.0;

	@OneToOne
	@JoinColumn(name = "f_flow_property")
	public FlowProperty flowProperty;

	public static FlowPropertyFactor of(FlowProperty prop) {
		return of(prop, 1.0);
	}

	public static FlowPropertyFactor of(FlowProperty prop, double factor) {
		var f = new FlowPropertyFactor();
		f.flowProperty = prop;
		f.conversionFactor = factor;
		return f;
	}

	@Override
	public FlowPropertyFactor copy() {
		var copy = new FlowPropertyFactor();
		copy.conversionFactor = conversionFactor;
		copy.flowProperty = flowProperty;
		return copy;
	}

}
