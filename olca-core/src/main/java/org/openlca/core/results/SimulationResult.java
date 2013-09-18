package org.openlca.core.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.matrix.FlowIndex;
import org.openlca.core.matrix.LongIndex;

public class SimulationResult {

	private FlowIndex flowIndex;
	private LongIndex impactIndex;
	private List<Double>[] flowResults;
	private List<Double>[] categoryResults;

	@SuppressWarnings("unchecked")
	public SimulationResult(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
		flowResults = new List[flowIndex.size()];
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	public boolean hasImpactResults() {
		return impactIndex != null && !impactIndex.isEmpty();
	}

	@SuppressWarnings("unchecked")
	public void setImpactIndex(LongIndex index) {
		this.impactIndex = index;
		if (index != null)
			categoryResults = new List[index.size()];
	}

	public LongIndex getImpactIndex() {
		return impactIndex;
	}

	public void appendFlowResults(double[] vector) {
		appendResults(vector, flowResults);
	}

	public void appendImpactResults(double[] vector) {
		appendResults(vector, categoryResults);
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
		return categoryResults[idx];
	}

}
