package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRows("Raw materials")
public class RawMaterial extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.RESOURCES;
	}

}
