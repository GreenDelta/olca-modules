package org.openlca.core.matrix;

import org.openlca.core.model.FlowType;
import org.openlca.core.model.UncertaintyType;

public class CalcExchange {

	public long processId;
	public long flowId;
	public long exchangeId;
	public boolean input;
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
	public boolean avoidedProduct;

	public double costValue;
	public long costCategory;

}
