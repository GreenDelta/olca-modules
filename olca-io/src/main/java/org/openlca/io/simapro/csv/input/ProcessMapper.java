package org.openlca.io.simapro.csv.input;

import java.util.List;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Process;
import org.openlca.expressions.Scope;
import org.openlca.simapro.csv.refdata.CalculatedParameterRow;
import org.openlca.simapro.csv.refdata.InputParameterRow;

interface ProcessMapper {

	IDatabase db();

	RefData refData();

	Process process();

	Scope formulaScope();

	List<InputParameterRow> inputParameterRows();

	List<CalculatedParameterRow> calculatedParameterRows();

}
