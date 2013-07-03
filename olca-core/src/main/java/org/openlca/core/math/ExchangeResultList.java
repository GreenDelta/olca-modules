package org.openlca.core.math;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.UnitGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a list of exchanges for a given matrix result.
 */
public class ExchangeResultList {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String ownerId;
	private Flow refFlow;
	private double refAmount;

	private ExchangeResultList(IDatabase database) {
	}

	public static ExchangeResultList on(IDatabase database) {
		return new ExchangeResultList(database);
	}

	public ExchangeResultList withOwner(String ownerId) {
		this.ownerId = ownerId;
		return this;
	}

	public ExchangeResultList withReferenceFlow(Flow flow, double amount) {
		this.refFlow = flow;
		this.refAmount = amount;
		return this;
	}

	public List<Exchange> create(FlowIndex flowIndex, double[] results) {
		List<Exchange> exchanges = new ArrayList<>();
		for (int i = 0; i < results.length; i++) {
			Flow flow = flowIndex.getFlowAt(i);
			double amount = results[i];
			if (amount == 0)
				continue;
			boolean input = flow.getFlowType() == FlowType.ELEMENTARY_FLOW ? flowIndex
					.isInput(flow) : results[i] < 0;
			if (flow.getFlowType() == FlowType.ELEMENTARY_FLOW) {
				amount *= input ? -1 : 1;
			} else {
				amount = Math.abs(amount);
			}
			Exchange exchange = createExchange(flow, input, amount);
			exchanges.add(exchange);
		}
		if (refFlow != null)
			exchanges.add(createExchange(refFlow, false, refAmount));
		return exchanges;
	}

	private Exchange createExchange(Flow flow, boolean input, double amount) {
		Exchange exchange = new Exchange(ownerId);
		exchange.setRefId(UUID.randomUUID().toString());
		exchange.setFlow(flow);
		exchange.setInput(input);
		exchange.getResultingAmount().setValue(amount);
		exchange.getResultingAmount().setFormula(Double.toString(amount));
		appendFlowInformation(exchange);
		return exchange;
	}

	private void appendFlowInformation(Exchange exchange) {
		try {
			Flow flow = exchange.getFlow();
			FlowPropertyFactor factor = flow.getReferenceFactor();
			exchange.setFlowPropertyFactor(factor);
			UnitGroup unitGroup = factor.getFlowProperty().getUnitGroup();
			exchange.setUnit(unitGroup.getReferenceUnit());
		} catch (final Exception e) {
			log.error("Loading reference flow property failed", e);
		}
	}

}
