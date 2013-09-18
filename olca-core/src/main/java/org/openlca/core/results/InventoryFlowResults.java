package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openlca.core.database.EntityCache;
import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.model.descriptors.FlowDescriptor;

/**
 * A result generator class for inventory flow results.
 */
public final class InventoryFlowResults {

	private FlowIndex index;
	private final InventoryResult result;

	public InventoryFlowResults(InventoryResult result) {
		this.result = result;
		this.index = result.getFlowIndex();
	}

	public Set<FlowDescriptor> getFlows(EntityCache cache) {
		return Results.getFlowDescriptors(result.getFlowIndex(), cache);
	}

	/**
	 * Returns the flow results of the inventory results. *No* entries are
	 * generated for 0-values.
	 */
	public List<InventoryFlowResult> getAll(EntityCache cache) {
		List<InventoryFlowResult> results = new ArrayList<>();
		for (FlowDescriptor d : getFlows(cache)) {
			double val = result.getFlowResult(d.getId());
			if (val == 0)
				continue;
			InventoryFlowResult r = new InventoryFlowResult();
			r.setFlow(d);
			r.setInput(index.isInput(d.getId()));
			r.setValue(val);
			results.add(r);
		}
		return results;
	}

	public InventoryFlowResult get(FlowDescriptor flow) {
		long flowId = flow.getId();
		InventoryFlowResult r = new InventoryFlowResult();
		r.setFlow(flow);
		r.setInput(index.isInput(flowId));
		double val = result.getFlowResult(flow.getId());
		r.setValue(val);
		return r;
	}
}
