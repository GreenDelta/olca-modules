package org.openlca.core.results;

import java.util.Collections;
import java.util.Set;

import org.openlca.core.database.Cache;
import org.openlca.core.matrices.LongIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class SimulationResults {

	public static Set<FlowDescriptor> getFlows(SimulationResult result,
			Cache cache) {
		return Results.getFlowDescriptors(result.getFlowIndex(), cache);
	}

	public static Set<ImpactCategoryDescriptor> getImpacts(
			SimulationResult result, Cache cache) {
		LongIndex impactIndex = result.getImpactIndex();
		if (impactIndex == null)
			return Collections.emptySet();
		return Results.getImpactDescriptors(impactIndex, cache);
	}
}
