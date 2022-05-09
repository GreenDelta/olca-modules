package org.openlca.util;

import java.util.ArrayList;
import java.util.Objects;

import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Process;

record AllocationCleanup(Process process) {

	void run() {
		var products = AllocationUtils.getProviderFlows(process);
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

	private boolean isQuantitativeReference(Exchange e) {
		return e != null
			&& Objects.equals(e, process.quantitativeReference);
	}

	private void checkFactors(Exchange product) {
		if (product == null)
			return;
		double defaultValue = 0;
		if (process.allocationFactors.isEmpty()
			&& isQuantitativeReference(product)) {
			defaultValue = 1; // initialize quant. ref. with 1
		}
		checkFactor(product, AllocationMethod.PHYSICAL, defaultValue);
		checkFactor(product, AllocationMethod.ECONOMIC, defaultValue);
		for (var exchange : process.exchanges) {
			if (Exchanges.isProviderFlow(exchange))
				continue;
			checkFactor(product, exchange, defaultValue);
		}
	}

	private void removeInvalid() {
		for (var factor : new ArrayList<>(process.allocationFactors)) {
			if (isValid(factor))
				continue;
			process.allocationFactors.remove(factor);
		}
	}

	private boolean isValid(AllocationFactor f) {
		if (f.method == null)
			return false;
		if (!hasExchangeWithFlow(f.productId))
			return false;
		if (f.method == AllocationMethod.CAUSAL && f.exchange == null)
			return false;
		return f.method != AllocationMethod.CAUSAL || hasExchangeFor(f.exchange.id);
	}

	private boolean hasExchangeFor(long id) {
		for (var exchange : process.exchanges)
			if (exchange.id == id)
				return true;
		return false;
	}

	private boolean hasExchangeWithFlow(long id) {
		for (var exchange : process.exchanges) {
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

	private void checkFactor(
		Exchange product, Exchange exchange, AllocationMethod type, double defaultValue) {
		var factor = findFactorFor(product, exchange, type);
		if (factor != null)
			return;
		factor = new AllocationFactor();
		factor.method = type;
		factor.productId = product.flow.id;
		factor.exchange = exchange;
		factor.value = defaultValue;
		process.allocationFactors.add(factor);
	}

	private AllocationFactor findFactorFor(
		Exchange product, Exchange exchange, AllocationMethod type) {
		if (type == AllocationMethod.CAUSAL && exchange == null)
			return null;
		for (var factor : process.allocationFactors) {
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
