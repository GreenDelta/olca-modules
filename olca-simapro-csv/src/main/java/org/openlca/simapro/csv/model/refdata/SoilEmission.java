package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRows("Emissions to soil")
public class SoilEmission extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.EMISSIONS_TO_SOIL;
	}

}
