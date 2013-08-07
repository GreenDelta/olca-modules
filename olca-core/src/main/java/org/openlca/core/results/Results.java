package org.openlca.core.results;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.indices.FlowIndex;
import org.openlca.core.indices.LongIndex;
import org.openlca.core.indices.LongPair;
import org.openlca.core.indices.ProductIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

/**
 * A helper class for generating result data.
 */
final class Results {

	public static Set<ProcessDescriptor> getProcessDescriptors(
			ProductIndex index, Cache cache) {
		if (index == null)
			return Collections.emptySet();
		HashSet<ProcessDescriptor> descriptors = new HashSet<>();
		for (int i = 0; i < index.size(); i++) {
			LongPair processProduct = index.getProductAt(i);
			long processId = processProduct.getFirst();
			ProcessDescriptor process = cache.getProcessDescriptor(processId);
			if (process != null)
				descriptors.add(process);
		}
		return descriptors;
	}

	public static Set<FlowDescriptor> getFlowDescriptors(FlowIndex index,
			Cache cache) {
		if (index == null)
			return Collections.emptySet();
		HashSet<FlowDescriptor> descriptors = new HashSet<>();
		for (int i = 0; i < index.size(); i++) {
			long flowId = index.getFlowAt(i);
			FlowDescriptor flow = cache.getFlowDescriptor(flowId);
			if (flow != null)
				descriptors.add(flow);
		}
		return descriptors;
	}

	public static Set<ImpactCategoryDescriptor> getImpactDescriptors(
			LongIndex index, Cache cache) {
		if (index == null)
			return Collections.emptySet();
		HashSet<ImpactCategoryDescriptor> descriptors = new HashSet<>();
		for (long id : index.getKeys()) {
			ImpactCategoryDescriptor impact = cache
					.getImpactCategoryDescriptor(id);
			descriptors.add(impact);
		}
		return descriptors;
	}

}
