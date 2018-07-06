package org.openlca.io.xls.results.system;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.SimpleResultProvider;
import org.openlca.io.xls.results.Sort;

class Util {

	static List<FlowDescriptor> flows(SimpleResultProvider<?> result) {
		Set<FlowDescriptor> set = result.getFlowDescriptors();
		return Sort.flows(set, result.cache);
	}

	static List<ProcessDescriptor> processes(
			SimpleResultProvider<?> result) {
		Set<ProcessDescriptor> procs = result.getProcessDescriptors();
		long refProcessId = result.result.productIndex.getRefFlow().getFirst();
		return Sort.processes(procs, refProcessId);
	}

	static List<ImpactCategoryDescriptor> impacts(
			SimpleResultProvider<?> result) {
		if (!result.hasImpactResults())
			return Collections.emptyList();
		Set<ImpactCategoryDescriptor> set = result.getImpactDescriptors();
		return Sort.impacts(set);
	}
}
