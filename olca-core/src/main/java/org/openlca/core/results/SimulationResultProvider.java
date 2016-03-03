package org.openlca.core.results;

import org.openlca.core.database.EntityCache;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

import java.util.ArrayList;
import java.util.List;

public class SimulationResultProvider<T extends SimulationResult> extends
		BaseResultProvider<T> {

	public SimulationResultProvider(T result, EntityCache cache) {
		super(result, cache);
	}

	public List<Double> getFlowResults(FlowDescriptor flow) {
		List<Double> rawResults = result.getFlowResults(flow.getId());
		List<Double> results = new ArrayList<>(rawResults.size());
		for (Double rawResult : rawResults)
			results.add(adoptFlowResult(rawResult, flow.getId()));
		return results;
	}

	/**
	 * Switches the sign for input-flows.
	 */
	protected double adoptFlowResult(double value, long flowId) {
		if (value == 0)
			return 0; // avoid -0 in the results
		boolean inputFlow = result.flowIndex.isInput(flowId);
		return inputFlow ? -value : value;
	}

	public List<Double> getImpactResults(ImpactCategoryDescriptor impact) {
		return result.getImpactResults(impact.getId());
	}

	public int getNumberOfRuns() {
		return result.getNumberOfRuns();
	}
}
