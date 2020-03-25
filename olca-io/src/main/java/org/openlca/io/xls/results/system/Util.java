package org.openlca.io.xls.results.system;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.model.NwSet;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.SimpleResult;
import org.openlca.io.xls.results.Sort;

class Util {

	static List<FlowDescriptor> flows(SimpleResult result, EntityCache cache) {
		Set<FlowDescriptor> set = result.getFlows();
		return Sort.flows(set, cache);
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
	
	static NwSet nwSet(CalculationSetup setup, EntityCache cache) {
		if (setup.nwSet == null)
			return null;
		return cache.get(NwSet.class, setup.nwSet.id);
	}

}
