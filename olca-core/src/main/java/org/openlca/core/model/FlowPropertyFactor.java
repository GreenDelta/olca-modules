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
public class FlowPropertyFactor extends AbstractEntity {

	@Column(name = "conversion_factor")
	public double conversionFactor = 1d;

	@OneToOne
	@JoinColumn(name = "f_flow_property")
	public FlowProperty flowProperty;

	@Override
	public FlowPropertyFactor clone() {
		final FlowPropertyFactor factor = new FlowPropertyFactor();
		factor.conversionFactor = conversionFactor;
		factor.flowProperty = flowProperty;
		return factor;
	}

}
