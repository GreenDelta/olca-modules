package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRows("Final waste flows")
public class FinalWasteFlow extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.FINAL_WASTE_FLOWS;
	}

}