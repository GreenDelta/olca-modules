package org.openlca.io.ilcd.input;

import org.openlca.core.model.ResultModel;
import org.openlca.ilcd.processes.Process;
import org.openlca.ilcd.util.Processes;
import org.openlca.util.Strings;

public class EpdImport {

	private final ImportConfig config;

	public EpdImport(ImportConfig config) {
		this.config = config;
	}

	public ResultModel run(Process dataSet) {
		var result = config.db().get(ResultModel.class, dataSet.getUUID());
		return result != null
			? result
			: createNew(dataSet);
	}

	private ResultModel createNew(Process dataSet) {
		var result = new ResultModel();
		result.refId = dataSet.getUUID();
		result.name = Strings.cut(
			Processes.fullName(dataSet, config.langOrder()), 2024);
		var info = Processes.getDataSetInfo(dataSet);
		if (info != null) {
			result.description = config.str(info.comment);
		}

		return config.db().insert(result);
	}


}
