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
		for (Exchange exchange : process.getExchanges()) {
			if (!isProduct(exchange))
				continue;
			products.add(exchange);
		}
		return products;
	}

	private boolean isProduct(Exchange exchange) {
		if (exchange.flow == null)
			return false;
		if (exchange.isInput && exchange.flow.getFlowType() == FlowType.WASTE_FLOW)
			return true;
		if (!exchange.isInput && exchange.flow.getFlowType() == FlowType.PRODUCT_FLOW)
			return true;
		return false;
	}

	private boolean isQuantitativeReference(Exchange exchange) {
		return exchange.equals(process.getQuantitativeReference());
	}

	private void run() {
		List<Exchange> products = getProducts();
		if (products.size() < 2) {
			process.getAllocationFactors().clear();
			return;
		}
		removeInvalid();
		if (process.getAllocationFactors().isEmpty()) {
			checkFactors(process.getQuantitativeReference());
		}
		for (Exchange product : products) {
			checkFactors(product);
		}
	}

	private void checkFactors(Exchange product) {
		double defaultValue = 0;
		if (process.getAllocationFactors().isEmpty() && isQuantitativeReference(product)) {
			defaultValue = 1; // initialize quant. ref. with 1
		}
		checkFactor(product, AllocationMethod.PHYSICAL, defaultValue);
		checkFactor(product, AllocationMethod.ECONOMIC, defaultValue);
		for (Exchange exchange : process.getExchanges()) {
			if (isProduct(exchange))
				continue;
			checkFactor(product, exchange, defaultValue);
		}
	}

	private void removeInvalid() {
		for (AllocationFactor factor : new ArrayList<>(process.getAllocationFactors())) {
			if (isValid(factor))
				continue;
			process.getAllocationFactors().remove(factor);
		}
	}

	private boolean isValid(AllocationFactor factor) {
		if (factor.getAllocationType() == null)
			return false;
		if (!hasExchangeWithFlow(factor.getProductId()))
			return false;
		if (factor.getAllocationType() == AllocationMethod.CAUSAL && factor.getExchange() == null)
			return false;
		if (factor.getAllocationType() == AllocationMethod.CAUSAL && !hasExchangeFor(factor.getExchange().getId()))
			return false;
		return true;
	}

	private boolean hasExchangeFor(long id) {
		for (Exchange exchange : process.getExchanges())
			if (exchange.getId() == id)
				return true;
		return false;
	}

	private boolean hasExchangeWithFlow(long id) {
		for (Exchange exchange : process.getExchanges()) {
			if (exchange.flow == null)
				continue;
			if (exchange.flow.getId() == id)
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
		factor.setAllocationType(type);
		factor.setProductId(product.flow.getId());
		factor.setExchange(exchange);
		factor.setValue(defaultValue);
		process.getAllocationFactors().add(factor);
	}

	private AllocationFactor findFactorFor(Exchange product, Exchange exchange, AllocationMethod type) {
		if (type == AllocationMethod.CAUSAL && exchange == null)
			return null;
		for (AllocationFactor factor : process.getAllocationFactors()) {
			if (factor.getAllocationType() != type)
				continue;
			if (product.flow == null || factor.getProductId() != product.flow.getId())
				continue;
			if (exchange != null && !factor.getExchange().equals(exchange))
				continue;
			return factor;
		}
		return null;
	}

}
