package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowPropertyType;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;

public class AllocationSwitch {

	private AllocationMethod method;

	public AllocationSwitch(AllocationMethod method) {
		this.method = method;
	}

	/**
	 * Get a map with outputs to technosphere and their respective allocation
	 * factors.
	 */
	public Map<Exchange, Double> getCommonFactors(Process process) {
		List<Exchange> techOutputs = getTechOutputs(process);
		FlowProperty prop = getCommonProperty(techOutputs);
		if (prop == null)
			return Collections.emptyMap();
		Map<Exchange, Double> amounts = new HashMap<>();
		double totalAmount = 0;
		for (Exchange product : techOutputs) {
			double rawAmount = product.getConvertedResult();
			Flow flow = product.getFlow();
			FlowPropertyFactor factor = flow.getFactor(prop);
			if (factor == null)
				continue;
			double amount = rawAmount / factor.getConversionFactor();
			totalAmount += amount;
			amounts.put(product, amount);
		}
		return makeRelative(amounts, totalAmount);
	}

	private Map<Exchange, Double> makeRelative(Map<Exchange, Double> amounts,
			double totalAmount) {
		if (totalAmount == 0)
			return amounts;
		Map<Exchange, Double> map = new HashMap<>();
		for (Exchange exchange : amounts.keySet()) {
			double amount = amounts.get(exchange);
			map.put(exchange, amount / totalAmount);
		}
		return map;
	}

	public FlowProperty getCommonProperty(Process process) {
		if (process == null)
			return null;
		List<Exchange> techOutputs = getTechOutputs(process);
		return getCommonProperty(techOutputs);
	}

	private FlowProperty getCommonProperty(List<Exchange> products) {
		List<FlowProperty> candidates = null;
		for (Exchange product : products) {
			Flow flow = product.getFlow();
			List<FlowProperty> props = getProperties(flow);
			if (candidates == null)
				candidates = props;
			else
				candidates.retainAll(props);
		}
		if (candidates == null || candidates.isEmpty())
			return null;
		return candidates.get(0);
	}

	private List<FlowProperty> getProperties(Flow flow) {
		List<FlowProperty> properties = new ArrayList<>();
		for (FlowPropertyFactor factor : flow.getFlowPropertyFactors()) {
			FlowProperty prop = factor.getFlowProperty();
			if (match(prop.getFlowPropertyType()))
				properties.add(prop);
		}
		return properties;
	}

	private boolean match(FlowPropertyType propertyType) {
		if (propertyType == null || method == null)
			return false;
		else if (propertyType == FlowPropertyType.ECONOMIC
				&& method == AllocationMethod.Economic)
			return true;
		else if (propertyType == FlowPropertyType.PHYSICAL
				&& method == AllocationMethod.Physical)
			return true;
		else
			return false;
	}

	/**
	 * Get the product or waste outputs. If the size of this list is 1 no
	 * allocation needs to be applied.
	 */
	public List<Exchange> getTechOutputs(Process process) {
		List<Exchange> outputs = new ArrayList<>();
		for (Exchange exchange : process.getExchanges()) {
			if (exchange.isInput() || exchange.getFlow() == null)
				continue;
			FlowType flowType = exchange.getFlow().getFlowType();
			if (flowType == FlowType.PRODUCT_FLOW
					|| flowType == FlowType.WASTE_FLOW)
				outputs.add(exchange);
		}
		return outputs;
	}

}
