package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;
import org.openlca.core.matrix.ProductIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;

public class BaseResultProvider<T extends BaseResult> {

	protected T result;
	protected EntityCache cache;

	public BaseResultProvider(T result, EntityCache cache) {
		this.result = result;
		this.cache = cache;
	}

	public T getResult() {
		return result;
	}

	public Set<ProcessDescriptor> getProcessDescriptors() {
		ProductIndex index = result.getProductIndex();
		if (index == null)
			return Collections.emptySet();
		Map<Long, ProcessDescriptor> values = cache.getAll(
				ProcessDescriptor.class, index.getProcessIds());
		HashSet<ProcessDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	public Set<FlowDescriptor> getFlowDescriptors() {
		FlowIndex index = result.getFlowIndex();
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>(index.size());
		for (long id : index.getFlowIds())
			ids.add(id);
		Map<Long, FlowDescriptor> values = cache.getAll(FlowDescriptor.class,
				ids);
		HashSet<FlowDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	public Set<ImpactCategoryDescriptor> getImpactDescriptors() {
		LongIndex index = result.getImpactIndex();
		if (index == null)
			return Collections.emptySet();
		List<Long> ids = new ArrayList<>(index.size());
		for (long id : index.getKeys())
			ids.add(id);
		Map<Long, ImpactCategoryDescriptor> values = cache.getAll(
				ImpactCategoryDescriptor.class, ids);
		HashSet<ImpactCategoryDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

}
