package org.openlca.simapro.csv.model.refdata;

import java.util.List;

import org.openlca.simapro.csv.model.enums.ElementaryFlowType;

public interface IElementaryFlowBlock {

	ElementaryFlowType getFlowType();

	List<ElementaryFlowRow> getFlows();

}
