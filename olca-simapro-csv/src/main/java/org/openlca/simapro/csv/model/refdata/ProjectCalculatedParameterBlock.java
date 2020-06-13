package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ParameterType;

@BlockModel("Project Calculated parameters")
public class ProjectCalculatedParameterBlock implements CalculatedParameterBlock {

	@BlockRows
	private List<CalculatedParameterRow> parameters = new ArrayList<>();

	@Override
	public ParameterType type() {
		return ParameterType.PROJECT;
	}

	@Override
	public List<CalculatedParameterRow> rows() {
		return parameters;
	}

}
