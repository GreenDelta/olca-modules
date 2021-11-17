package org.openlca.core.matrix;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.util.Strings;
import org.slf4j.LoggerFactory;

public class CalcExchange {

	public long processId;
	public long flowId;
	public long exchangeId;
	public boolean isInput;

	public double conversionFactor;
	public double amount;
	public String formula;

	public UncertaintyType uncertaintyType;
	public double parameter1;
	public double parameter2;
	public double parameter3;

	public FlowType flowType;

	/**
	 * 0 if the exchange has no default provider.
	 */
	public long defaultProviderId;

	/**
	 * 0 if the exchange has no location assigned.
	 */
	public long locationId;

	public boolean isAvoided;

	public double costValue;

	/**
	 * A conversion factor for converting the costs value into the reference
	 * currency of the database.
	 */
	public double currencyFactor;

	public String costFormula;

	/**
	 * Returns true when this exchange can be allocated to a product output or
	 * waste input, thus it is either a product input, waste output, or
	 * elementary flow (and not an avoided product or waste flow).
	 */
	public boolean isAllocatable() {
		if (isAvoided || flowType == null)
			return false;
		return switch (flowType) {
			case ELEMENTARY_FLOW -> true;
			case PRODUCT_FLOW -> isInput;
			case WASTE_FLOW -> !isInput;
		};
	}

	/**
	 * Returns true when the exchange has an uncertainty distribution assigned.
	 */
	public boolean hasUncertainty() {
		return uncertaintyType != null && uncertaintyType != UncertaintyType.NONE;
	}

	/**
	 * Returns true if the flow of this exchange is an elementary flow.
	 */
	public boolean isElementary() {
		return flowType == null || flowType == FlowType.ELEMENTARY_FLOW;
	}

	/**
	 * Returns true if this exchange can be linked to a provider. This is the
	 * case if this exchange is a product input or waste output.
	 */
	public boolean isLinkable() {
		return (isInput && flowType == FlowType.PRODUCT_FLOW)
			|| (!isInput && flowType == FlowType.WASTE_FLOW);
	}

	public double matrixValue(FormulaInterpreter interpreter,
		double allocationFactor) {

		double a = amount;
		if (Strings.notEmpty(formula) && interpreter != null) {
			try {
				var scope = interpreter.getScopeOrGlobal(processId);
				a = scope.eval(formula);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("Formula evaluation failed, exchange "
					+ exchangeId, e);
			}
		}

		a *= (conversionFactor * allocationFactor);
		if (isAvoided) {
			// avoided product or waste flows
			if (flowType == FlowType.PRODUCT_FLOW)
				return a;
			if (flowType == FlowType.WASTE_FLOW)
				return -a;
		}
		return isInput ? -a : a;
	}

	public double costValue(FormulaInterpreter interpreter,
		double allocationFactor) {

		double c = costValue;
		if (Strings.notEmpty(costFormula) && interpreter != null) {
			try {
				var scope = interpreter.getScopeOrGlobal(processId);
				c = scope.eval(costFormula);
			} catch (Exception e) {
				var log = LoggerFactory.getLogger(getClass());
				log.error("Formula evaluation for costs failed, exchange "
					+ exchangeId, e);
			}
		}

		c *= (allocationFactor * currencyFactor);
		if (flowType == FlowType.PRODUCT_FLOW && !isInput)
			return -c; // product outputs -> revenues
		if (flowType == FlowType.WASTE_FLOW && isInput)
			return -c; // waste inputs -> revenues
		return c;
	}

}
