package org.openlca.simapro.csv.model.refdata;

import java.util.List;

import org.openlca.simapro.csv.model.CalculatedParameterRow;
import org.openlca.simapro.csv.model.enums.ParameterType;

public interface CalculatedParameterBlock {

	ParameterType type();

	List<CalculatedParameterRow> rows();

}
