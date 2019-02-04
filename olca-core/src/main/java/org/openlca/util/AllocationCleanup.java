package org.openlca.util;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;

public class AllocationCleanup {

	private final Process process;

	private AllocationCleanup(Process process) {
		this.process = process;
	}

	public static void on(Process process) {
		new AllocationCleanup(process).run();
	}

	private List<Exchange> getProducts() {
		List<Exchange> products = new ArrayList<>();
		for (Exchange exchange : process.exchanges) {
			if (!isProduct(exchange))
				continue;
			products.add(exchange);
		}
		return products;
	}

	private boolean isProduct(Exchange exchange) {
		if (exchange.flow == null)
			return false;
		if (exchange.isInput && exchange.flow.flowType == FlowType.WASTE_FLOW)
			return true;
		if (!exchange.isInput && exchange.flow.flowType == FlowType.PRODUCT_FLOW)
			return true;
		return false;
	}

	private boolean isQuantitativeReference(Exchange exchange) {
		return exchange.equals(process.quantitativeReference);
	}

	private void run() {
		List<Exchange> products = getProducts();
		if (products.size() < 2) {
			process.allocationFactors.clear();
			return;
		}
		removeInvalid();
		if (process.allocationFactors.isEmpty()) {
			checkFactors(process.quantitativeReference);
		}
		for (Exchange product : products) {
			checkFactors(product);
		}
	}

	private void checkFactors(Exchange product) {
		double defaultValue = 0;
		if (process.allocationFactors.isEmpty() && isQuantitativeReference(product)) {
			defaultValue = 1; // initialize quant. ref. with 1
		}
		checkFactor(product, AllocationMethod.PHYSICAL, defaultValue);
		checkFactor(product, AllocationMethod.ECONOMIC, defaultValue);
		for (Exchange exchange : process.exchanges) {
			if (isProduct(exchange))
				continue;
			checkFactor(product, exchange, defaultValue);
		}
	}

	private void removeInvalid() {
		for (AllocationFactor factor : new ArrayList<>(process.allocationFactors)) {
			if (isValid(factor))
				continue;
			process.allocationFactors.remove(factor);
		}
	}

	private boolean isValid(AllocationFactor factor) {
		if (factor.method == null)
			return false;
		if (!hasExchangeWithFlow(factor.productId))
			return false;
		if (factor.method == AllocationMethod.CAUSAL && factor.exchange == null)
			return false;
		if (factor.method == AllocationMethod.CAUSAL && !hasExchangeFor(factor.exchange.id))
			return false;
		return true;
	}

	private boolean hasExchangeFor(long id) {
		for (Exchange exchange : process.exchanges)
			if (exchange.id == id)
				return true;
		return false;
	}

	private boolean hasExchangeWithFlow(long id) {
		for (Exchange exchange : process.exchanges) {
			if (exchange.flow == null)
				continue;
			if (exchange.flow.id == id)
				return true;
		}
		return false;
	}

	private void checkFactor(Exchange product, AllocationMethod type, double defaultValue) {
		checkFactor(product, null, type, defaultValue);
	}

	private void checkFactor(Exchange product, Exchange exchange, double defaultValue) {
		checkFactor(product, exchange, AllocationMethod.CAUSAL, defaultValue);
	}

	private void checkFactor(Exchange product, Exchange exchange, AllocationMethod type, double defaultValue) {
		AllocationFactor factor = findFactorFor(product, exchange, type);
		if (factor != null)
			return;
		factor = new AllocationFactor();
		factor.method = type;
		factor.productId = product.flow.id;
		factor.exchange = exchange;
		factor.value = defaultValue;
		process.allocationFactors.add(factor);
	}

	private AllocationFactor findFactorFor(Exchange product, Exchange exchange, AllocationMethod type) {
		if (type == AllocationMethod.CAUSAL && exchange == null)
			return null;
		for (AllocationFactor factor : process.allocationFactors) {
			if (factor.method != type)
				continue;
			if (product.flow == null || factor.productId != product.flow.id)
				continue;
			if (exchange != null && !factor.exchange.equals(exchange))
				continue;
			return factor;
		}
		return null;
	}

}
