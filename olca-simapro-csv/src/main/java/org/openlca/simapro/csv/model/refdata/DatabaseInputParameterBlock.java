package org.openlca.simapro.csv.model.refdata;

import java.util.ArrayList;
import java.util.List;

import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.annotations.BlockModel;
import org.openlca.simapro.csv.model.annotations.BlockRows;
import org.openlca.simapro.csv.model.enums.ParameterType;

@BlockModel("Database Input parameters")
public class DatabaseInputParameterBlock implements IParameterBlock {

	@BlockRows
	private List<InputParameterRow> parameters = new ArrayList<>();

	@Override
	public ParameterType getParameterType() {
		return ParameterType.DATABASE;
	}

	public List<InputParameterRow> getParameters() {
		return parameters;
	}

}
