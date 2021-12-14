package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_result_flows")
public class ResultFlow  extends AbstractExchange
	implements Copyable<ResultFlow> {

	@Column(name = "origin")
	@Enumerated(EnumType.STRING)
	public ResultOrigin origin;

	@Override
	public ResultFlow copy() {
		var copy = new ResultFlow();
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
