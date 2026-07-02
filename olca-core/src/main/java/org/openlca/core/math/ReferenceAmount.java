package org.openlca.core.math;

import org.jspecify.annotations.Nullable;
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
	public static double get(@Nullable AbstractExchange e) {
		return e != null
			? get(e.amount, e.unit, e.flowPropertyFactor)
			: 0;
	}

	/// Converts the given amount from its current unit and flow property
	/// into the reference unit of the reference flow property.
	///
	/// The conversion follows the formula:
	///
	/// ```
	/// (amount * unitFactor) / propertyFactor
	/// ```
	///
	/// If the unit or property is `null`, they are treated as already being in
	/// the reference state.
	public static double get(
		double amount,
		@Nullable Unit unit,
		@Nullable FlowPropertyFactor property
	) {

		double refAmount = unit != null
			? amount * unit.conversionFactor
			: amount;

		if (property == null)
			return refAmount;

		return property.conversionFactor != 0
			? refAmount / property.conversionFactor
			: 0;
	}
}
