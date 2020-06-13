package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockModel("Non material emissions")
public class NonMaterialEmissionBlock implements ElementaryFlowBlock {

	@BlockRows
	private List<ElementaryFlowRow> flows = new ArrayList<>();

	@Override
	public List<ElementaryFlowRow> rows() {
		return flows;
	}

	@Override
	public ElementaryFlowType type() {
		return ElementaryFlowType.NON_MATERIAL_EMISSIONS;
	}

}