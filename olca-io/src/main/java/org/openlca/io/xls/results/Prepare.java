package org.openlca.io.xls.results;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.ContributionResultProvider;

public class Prepare {

	static List<FlowDescriptor> flows(ContributionResultProvider<?> result) {
		Set<FlowDescriptor> set = result.getFlowDescriptors();
		return Sort.flows(set, result.cache);
	}

	static List<ProcessDescriptor> processes(ContributionResultProvider<?> result) {
		Set<ProcessDescriptor> procs = result.getProcessDescriptors();
		long refProcessId = result.result.productIndex.getRefFlow().getFirst();
		return Sort.processes(procs, refProcessId);
	}

	static List<ImpactCategoryDescriptor> impacts(ContributionResultProvider<?> result) {
		if (!result.hasImpactResults())
			return Collections.emptyList();
		Set<ImpactCategoryDescriptor> set = result.getImpactDescriptors();
		return Sort.impacts(set);
	}
}
