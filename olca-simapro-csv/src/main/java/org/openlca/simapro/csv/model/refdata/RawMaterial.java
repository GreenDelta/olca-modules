package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRow("Raw materials")
public class RawMaterial extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.RESOURCES;
	}

}
