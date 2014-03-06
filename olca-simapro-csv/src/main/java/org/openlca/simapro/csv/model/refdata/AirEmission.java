package org.openlca.simapro.csv.model.refdata;

import org.openlca.simapro.csv.model.annotations.BlockRow;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockRow("Airborne emissions")
public class AirEmission extends SPElementaryFlow {

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.EMISSIONS_TO_AIR;
	}

}
