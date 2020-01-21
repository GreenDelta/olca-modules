package org.openlca.io.xls.results.system;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.Sort;

class Util {

	static List<IndexFlow> flows(SimpleResult result, EntityCache cache) {
		return Sort.flows(result.getFlows(), cache);
	}

	static List<CategorizedDescriptor> processes(SimpleResult result) {
		Set<CategorizedDescriptor> procs = result.getProcesses();
		long refProcessId = result.techIndex.getRefFlow().id();
		return Sort.processes(procs, refProcessId);
	}

	static List<ImpactCategoryDescriptor> impacts(SimpleResult result) {
		if (!result.hasImpactResults())
			return Collections.emptyList();
		Set<ImpactCategoryDescriptor> set = result.getImpacts();
		return Sort.impacts(set);
	}

}
