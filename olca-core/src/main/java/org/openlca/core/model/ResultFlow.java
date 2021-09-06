package org.openlca.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_result_flows")
public class ResultFlow  extends AbstractExchange {

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
		return clone;
	}
}
