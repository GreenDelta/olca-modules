package org.openlca.core.matrix;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;
import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalcExchange {

	public long processId;
	public long flowId;
	public long exchangeId;
	public boolean isInput;

	public double conversionFactor;
	public double amount;
	public String amountFormula;

	public UncertaintyType uncertaintyType;
	public double parameter1;
	public double parameter2;
	public double parameter3;

	// TODO: do we need formulas for uncertainty parameters?
	@Deprecated
	public String parameter1Formula;
	@Deprecated
	public String parameter2Formula;
	@Deprecated
	public String parameter3Formula;

	public FlowType flowType;

	/** 0 if the exchange has no default provider. */
	public long defaultProviderId;
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
		switch (flowType) {
		case ELEMENTARY_FLOW:
			return true;
		case PRODUCT_FLOW:
			return isInput;
		case WASTE_FLOW:
			return !isInput;
		default:
			// this should never happen
			return false;
		}
	}

	/**
	 * Returns true when the exchange has an uncertainty distribution assigned.
	 */
	public boolean hasUncertainty() {
		return uncertaintyType != null
				&& uncertaintyType != UncertaintyType.NONE;
	}

	public double matrixValue(
			FormulaInterpreter interpreter,
			double allocationFactor) {

		double a = amount;
		if (amountFormula != null && interpreter != null) {
			try {
				Scope scope = interpreter.getScope(processId);
				if (scope == null) {
					scope = interpreter.getGlobalScope();
				}
				a = scope.eval(amountFormula);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("Formula evaluation failed, exchange "
						+ exchangeId, e);
			}
		}

		a *= (conversionFactor * allocationFactor);
		if (!isAvoided) {
			return isInput ? -a : a;
		} else {
			// avoided product or waste flows
			if (flowType == FlowType.PRODUCT_FLOW)
				return a;
			if (flowType == FlowType.WASTE_FLOW)
				return -a;
			return isInput ? -a : a;
		}
	}

	public double costValue(
			FormulaInterpreter interpreter,
			double allocationFactor) {

		double c = costValue;
		if (costFormula != null && interpreter != null) {
			try {
				Scope scope = interpreter.getScope(processId);
				if (scope == null) {
					scope = interpreter.getGlobalScope();
				}
				c = scope.eval(costFormula);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
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
