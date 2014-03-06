package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRows("Airborne emissions")
public class AirEmission extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.EMISSIONS_TO_AIR;
	}

}
