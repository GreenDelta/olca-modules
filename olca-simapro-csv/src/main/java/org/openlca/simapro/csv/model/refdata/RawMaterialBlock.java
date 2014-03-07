package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

@BlockModel("Raw materials")
public class RawMaterialBlock implements IElementaryFlowBlock {

	@BlockRows
	private List<ElementaryFlowRow> flows = new ArrayList<>();

	@Override
	public List<ElementaryFlowRow> getFlows() {
		return flows;
	}

	@Override
	public ElementaryFlowType getFlowType() {
		return ElementaryFlowType.RESOURCES;
	}

}
