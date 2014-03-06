package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRow("Non material emissions")
public class NonMaterialEmission extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.NON_MATERIAL_EMISSIONS;
	}

}