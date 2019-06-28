package org.openlca.core.matrix;

import org.openlca.core.model.UncertaintyType;

public class CalcImpactFactor {

	public long imactCategoryId;
	public long flowId;
	public double conversionFactor;
	public double amount;
	public String formula;

	public UncertaintyType uncertaintyType;
	public double parameter1;
	public double parameter2;
	public double parameter3;

}
