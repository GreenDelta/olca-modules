package org.openlca.simapro.csv.model.refdata;

import java.util.List;

import org.openlca.simapro.csv.model.InputParameterRow;
import org.openlca.simapro.csv.model.enums.ParameterType;

public interface InputParameterBlock {

	ParameterType type();

	List<InputParameterRow> rows();

}
