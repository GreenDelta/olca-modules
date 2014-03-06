package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRow("Economic issues")
public class EconomicIssue extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.ECONOMIC_ISSUES;
	}

}
