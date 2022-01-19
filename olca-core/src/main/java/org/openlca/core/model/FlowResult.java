package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_flow_results")
public class FlowResult  extends AbstractExchange
	implements Copyable<FlowResult> {

	@Column(name = "origin")
	@Enumerated(EnumType.STRING)
	public ResultOrigin origin;

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
		copy.origin = origin;
		return copy;
	}
}
