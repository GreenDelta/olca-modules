package org.openlca.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;

/**
 * A common calculation reference of a set of allocation factors.
 */
public record AllocationRef(
	AllocationMethod method, FlowProperty property, boolean isCosts) {

	/**
	 * Defines a flow property as the calculation reference for allocation
	 * factors of the given type.
	 */
	public static AllocationRef of(AllocationMethod method, FlowProperty property) {
		return new AllocationRef(method, property, false);
	}

	/**
	 * Defines the costs or revenues of exchanges as the calculation reference
	 * for allocation factors of the given type.
	 */
	public static AllocationRef ofCosts(AllocationMethod method) {
		return new AllocationRef(method, null, true);
	}

	public boolean isEmpty() {
		return method == null || (property == null && !isCosts);
	}

	/**
	 * Calculates a set of calculation factors based on this calculation
	 * reference. The factors are not added to the process and the process is not
	 * modified.
	 */
	public List<AllocationFactor> apply(Process process) {
		var techFlows = AllocationUtils.getProviderFlows(process);
		if (techFlows == null || techFlows.size() < 2 || isEmpty())
			return Collections.emptyList();
		var factors = new ArrayList<AllocationFactor>();
		double total = 0;
		for (var e : techFlows) {
			if (e.flow == null)
				continue;
			var factor = new AllocationFactor();
			factor.method = method;
			factors.add(factor);
			factor.productId = e.flow.id;
			if (isCosts) {
				var currency = e.currency;
				var costs = e.costs;
				if (currency != null && costs != null) {
					factor.value = Math.abs(currency.conversionFactor * costs);
				}
			} else {
				var amount = ReferenceAmount.get(e);
				var propFactor = e.flow.getFactor(property);
				if (propFactor != null) {
					factor.value = Math.abs(propFactor.conversionFactor * amount);
				}
			}
			total += factor.value;
		}
		if (total != 0) {
			for (var factor : factors) {
				factor.value /= total;
			}
		}

		if (method != AllocationMethod.CAUSAL)
			return factors;

		// create causal factors
		var exchanges = AllocationUtils.getNonProviderFlows(process);
		var causals = new ArrayList<AllocationFactor>(
			factors.size() * exchanges.size());
		for (var e : exchanges) {
			for (var f : factors) {
				var causal = f.copy();
				causal.exchange = e;
				causals.add(causal);
			}
		}
		return causals;
	}
}

