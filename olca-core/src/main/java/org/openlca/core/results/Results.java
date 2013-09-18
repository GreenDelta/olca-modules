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

/**
 * A helper class for generating result data.
 */
final class Results {

	public static Set<ProcessDescriptor> getProcessDescriptors(
			ProductIndex index, EntityCache cache) {
		if (index == null)
			return Collections.emptySet();
		Map<Long, ProcessDescriptor> values = cache.getAll(
				ProcessDescriptor.class, index.getProcessIds());
		HashSet<ProcessDescriptor> descriptors = new HashSet<>();
		descriptors.addAll(values.values());
		return descriptors;
	}

	public static Set<FlowDescriptor> getFlowDescriptors(FlowIndex index,
			EntityCache cache) {
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

	public static Set<ImpactCategoryDescriptor> getImpactDescriptors(
			LongIndex index, EntityCache cache) {
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
