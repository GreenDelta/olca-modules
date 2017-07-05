package org.openlca.io.ilcd.output;

import java.util.Map;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.ilcd.processes.Exchange;

class AllocationFactors {

	private Process process;
	private Map<org.openlca.core.model.Exchange, Exchange> exchangeMap;

	private AllocationFactors(Process process,
			Map<org.openlca.core.model.Exchange, Exchange> exchangeMap) {
		this.process = process;
		this.exchangeMap = exchangeMap;
	}

	public static void map(Process process,
			Map<org.openlca.core.model.Exchange, Exchange> exchangeMap) {
		if (exchangeMap.isEmpty() || process.getAllocationFactors().isEmpty())
			return;
		new AllocationFactors(process, exchangeMap).map();

	}

	private void map() {
		for (AllocationFactor factor : process.getAllocationFactors()) {
			if (factor.getExchange() != null)
				addCausalFactor(factor);
			else
				addOtherFactor(factor);
		}
	}

	private void addCausalFactor(AllocationFactor factor) {
		Exchange exchange = exchangeMap.get(factor.getExchange());
		if (exchange == null)
			return;
		Exchange product = findProduct(factor);
		if (product == null)
			return;
		addFactor(exchange, factor.getValue(), product.id);
	}

	private void addOtherFactor(AllocationFactor factor) {
		if (factor.getAllocationType() != AllocationMethod.PHYSICAL)
			return;
		Exchange product = findProduct(factor);
		if (product == null)
			return;
		addFactor(product, factor.getValue(), product.id);
	}

	private void addFactor(Exchange iExchange, double factor, int ref) {
		org.openlca.ilcd.processes.AllocationFactor f = new org.openlca.ilcd.processes.AllocationFactor();
		f.fraction = factor * 100;
		f.productExchangeId = ref;
		iExchange.add(f);
	}

	private Exchange findProduct(AllocationFactor factor) {
		for (org.openlca.core.model.Exchange oExchange : exchangeMap.keySet()) {
			if (oExchange.isInput)
				continue;
			if (oExchange.flow == null)
				continue;
			if (oExchange.flow.getFlowType() != FlowType.PRODUCT_FLOW)
				continue;
			if (oExchange.flow.getId() == factor.getProductId())
				return exchangeMap.get(oExchange);
		}
		return null;
	}
}
