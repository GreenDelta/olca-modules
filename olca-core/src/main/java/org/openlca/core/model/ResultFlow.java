package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_result_flows")
public class ResultFlow  extends AbstractExchange {

	@Column(name = "origin")
	@Enumerated(EnumType.STRING)
	public ResultOrigin origin;

	@Override
	protected ResultFlow clone() {
		var clone = new ResultFlow();
		clone.amount = amount;
		clone.flow = flow;
		clone.flowPropertyFactor = flowPropertyFactor;
		clone.isInput = isInput;
		clone.unit = unit;
		clone.description = description;
		clone.location = location;
		clone.origin = origin;
		return clone;
	}
}
