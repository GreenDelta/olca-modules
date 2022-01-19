package org.openlca.core.math;

import org.openlca.core.model.AbstractExchange;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.core.model.Unit;

/**
 * Functions for getting the reference amount. The reference amount is the
 * amount of a flow value converted to the reference unit and flow property of
 * that flow. This is the value that is used in the calculations.
 */
public final class ReferenceAmount {

	private ReferenceAmount() {
	}

	/**
	 * Get the reference amount of the reference flow / quantitative reference
	 * of the given product system.
	 */
	public static double get(ProductSystem system) {
		if (system == null)
			return 0;
		return get(system.targetAmount,
			system.targetUnit,
			system.targetFlowPropertyFactor);
	}

	public static double get(Result result) {
		return result == null
			? 0
			: get(result.referenceFlow);
	}

	/**
	 * Get the reference amount of the given exchange.
	 */
	public static double get(AbstractExchange e) {
		if (e == null)
			return 0;
		return get(e.amount, e.unit, e.flowPropertyFactor);
	}

	public static double get(double amount, Unit unit,
		FlowPropertyFactor factor) {
		double refAmount = amount;
		if (unit != null) {
			refAmount = refAmount * unit.conversionFactor;
		}
		if (factor != null) {
			refAmount = refAmount / factor.conversionFactor;
		}
		return refAmount;
	}

}
