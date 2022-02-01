package org.openlca.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_flow_results")
public class FlowResult  extends AbstractExchange
	implements Copyable<FlowResult> {

	public static FlowResult outputOf(Flow flow, double amount) {
		return of(flow, amount, false);
	}

	public static FlowResult inputOf(Flow flow, double amount) {
		return of(flow, amount, true);
	}

	private static FlowResult of(
		Flow flow, double amount, boolean isInput) {
		var result = new FlowResult();
		result.amount = amount;
		result.isInput = isInput;
		if (flow != null) {
			result.flow = flow;
			result.flowPropertyFactor = flow.getReferenceFactor();
			result.unit = flow.getReferenceUnit();
		}
		return result;
	}

	@Override
	public FlowResult copy() {
		var copy = new FlowResult();
		copy.amount = amount;
		copy.flow = flow;
		copy.flowPropertyFactor = flowPropertyFactor;
		copy.isInput = isInput;
		copy.unit = unit;
		copy.description = description;
		copy.location = location;
		return copy;
	}
}
