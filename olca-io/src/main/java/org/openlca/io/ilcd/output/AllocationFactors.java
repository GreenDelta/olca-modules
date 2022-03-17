package org.openlca.io.ilcd.output;

import java.util.Map;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.ilcd.processes.Exchange;

class AllocationFactors {

	private final Process process;
	private final Map<org.openlca.core.model.Exchange, Exchange> exchangeMap;
	private final AllocationMethod defaultMethod;

	private AllocationFactors(Process process,
			Map<org.openlca.core.model.Exchange, Exchange> exchangeMap) {
		this.process = process;
		this.exchangeMap = exchangeMap;

		var method = process.defaultAllocationMethod;
		if (method != AllocationMethod.PHYSICAL
			&& method != AllocationMethod.ECONOMIC) {
			defaultMethod = AllocationMethod.PHYSICAL;
		} else {
			boolean hasFactors = false;
			for (var factor : process.allocationFactors) {
				if (factor.method == method) {
					hasFactors = true;
					break;
				}
			}
			defaultMethod = hasFactors
				? method
				: AllocationMethod.PHYSICAL;
		}

	}

	public static void map(Process process,
			Map<org.openlca.core.model.Exchange, Exchange> exchangeMap) {
		if (exchangeMap.isEmpty() || process.allocationFactors.isEmpty())
			return;
		new AllocationFactors(process, exchangeMap).map();

	}

	private void map() {
		for (var factor : process.allocationFactors) {
			if (factor.exchange != null)
				addCausalFactor(factor);
			else
				addOtherFactor(factor);
		}
	}

	private void addCausalFactor(AllocationFactor factor) {
		Exchange exchange = exchangeMap.get(factor.exchange);
		if (exchange == null)
			return;
		Exchange product = findProduct(factor);
		if (product == null)
			return;
		addFactor(exchange, factor.value, product.id);
	}

	private void addOtherFactor(AllocationFactor factor) {
		if (factor.method != defaultMethod)
			return;
		Exchange product = findProduct(factor);
		if (product == null)
			return;
		addFactor(product, factor.value, product.id);
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
			if (oExchange.flow.flowType != FlowType.PRODUCT_FLOW)
				continue;
			if (oExchange.flow.id == factor.productId)
				return exchangeMap.get(oExchange);
		}
		return null;
	}
}
