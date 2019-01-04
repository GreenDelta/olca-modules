package org.openlca.core.matrix;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;

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
	public String parameter1Formula;
	public String parameter2Formula;
	public String parameter3Formula;
	public FlowType flowType;

	/** 0 if the exchange has no default provider. */
	public long defaultProviderId;
	public boolean isAvoided;

	public double costValue;
	public String costFormula;
	public long currency;

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
}
