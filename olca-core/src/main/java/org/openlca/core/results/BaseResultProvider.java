package org.openlca.core.results;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class BaseResultProvider<T extends BaseResult>
		implements IResultProvider {

	public final T result;
	public final EntityCache cache;

	public BaseResultProvider(T result, EntityCache cache) {
		this.result = result;
		this.cache = cache;
	}

	@Override
	public boolean hasImpactResults() {
		return result.hasImpactResults();
	}

	@Override
	public boolean hasCostResults() {
		return result.hasCostResults;
	}

	@Override
	public Set<ProcessDescriptor> getProcessDescriptors() {
		TechIndex index = result.techIndex;
		if (index == null)
			return Collections.emptySet();
		Map<Long, ProcessDescriptor> values = cache.getAll(
				ProcessDescriptor.class, index.getProcessIds());
		HashSet<ProcessDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	@Override
	public boolean isInput(FlowDescriptor flow) {
		if (flow == null || result.flowIndex == null)
			return false;
		return result.flowIndex.isInput(flow.getId());
	}
}
