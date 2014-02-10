package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The results of a Monte-Carlo-Simulation. The single result values of the
 * simulation runs are stored in an array of lists where the flow- and LCIA
 * category indices are mapped to the respective array rows and the result
 * values to the respective list entries.
 */
public class SimulationResult extends BaseResult {

	private List<Double>[] flowResults;
	private List<Double>[] impactResults;

	public List<Double>[] getFlowResults() {
		return flowResults;
	}

	public void setFlowResults(List<Double>[] flowResults) {
		this.flowResults = flowResults;
	}

	public List<Double>[] getImpactResults() {
		return impactResults;
	}

	public void setImpactResults(List<Double>[] impactResults) {
		this.impactResults = impactResults;
	}

	@SuppressWarnings("unchecked")
	public void appendFlowResults(double[] vector) {
		if (flowResults == null)
			flowResults = new List[flowIndex.size()];
		appendResults(vector, flowResults);
	}

	@SuppressWarnings("unchecked")
	public void appendImpactResults(double[] vector) {
		if (impactResults == null)
			impactResults = new List[impactIndex.size()];
		appendResults(vector, impactResults);
	}

	private void appendResults(double[] vector, List<Double>[] results) {
		for (int i = 0; i < vector.length; i++) {
			List<Double> list = results[i];
			if (list == null) {
				list = new ArrayList<>();
				results[i] = list;
			}
			list.add(vector[i]);
		}
	}

	public List<Double> getFlowResults(long flowId) {
		int idx = flowIndex.getIndex(flowId);
		if (idx < 0)
			return Collections.emptyList();
		return flowResults[idx];
	}

	public List<Double> getImpactResults(long impactCategoryId) {
		if (impactIndex == null)
			return Collections.emptyList();
		int idx = impactIndex.getIndex(impactCategoryId);
		if (idx < 0)
			return Collections.emptyList();
		return impactResults[idx];
	}

	public int getNumberOfRuns() {
		if (flowResults == null || flowResults.length == 0)
			return 0;
		List<Double> first = flowResults[0];
		if (first == null)
			return 0;
		else
			return first.size();
	}

}
