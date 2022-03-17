package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

/**
 * Contains the information of the declared product of an EPD.
 */
@Embeddable
public class EpdProduct implements Copyable<EpdProduct> {

	@OneToOne
	@JoinColumn(name = "f_flow")
	public Flow flow;

	@OneToOne
	@JoinColumn(name = "f_flow_property")
	public FlowProperty property;

	@OneToOne
	@JoinColumn(name = "f_unit")
	public Unit unit;

	@Column(name = "amount")
	public double amount;

	public static EpdProduct of(Flow flow, double amount) {
		var product = new EpdProduct();
		product.flow = flow;
		product.amount = amount;
		if (flow != null) {
			product.property = flow.referenceFlowProperty;
			product.unit = flow.getReferenceUnit();
		}
		return product;
	}

	@Override
	public EpdProduct copy() {
		var copy = new EpdProduct();
		copy.flow = flow;
		copy.property = property;
		copy.unit = unit;
		copy.amount = amount;
		return copy;
	}

}
