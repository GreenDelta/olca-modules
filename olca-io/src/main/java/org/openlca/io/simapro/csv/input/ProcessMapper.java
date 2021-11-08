package org.openlca.io.simapro.csv.input;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
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

	/**
	 * Tries to infer the category and location of the process from the reference
	 * flow of the process. It directly sets these attributes to the underlying
	 * process if these attributes can be found. It is important that this method
	 * is called after the exchanges of the process are mapped.
	 */
	default void inferCategoryAndLocation() {
		var qref = process().quantitativeReference;
		if (qref == null || qref.flow == null)
			return;
		var flow = qref.flow;
		process().location = flow.location;
		if (flow.category == null)
			return;
		var path = new ArrayList<String>();
		var c = flow.category;
		while (c != null) {
			path.add(0, c.name);
			c = c.category;
		}
		if (path.isEmpty())
			return;
		process().category = CategoryDao.sync(
			db(), ModelType.PROCESS, path.toArray(String[]::new));
	}
}
