package org.openlca.core.math;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.core.model.Flow;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

public class SimulationResult implements IResultData {

	private FlowIndex flowIndex;
	private Index<ImpactCategoryDescriptor> categoryIndex;
	private List<Double>[] flowResults;
	private List<Double>[] categoryResults;

	@SuppressWarnings("unchecked")
	SimulationResult(FlowIndex flowIndex) {
		this.flowIndex = flowIndex;
		flowResults = new List[flowIndex.size()];
	}

	public FlowIndex getFlowIndex() {
		return flowIndex;
	}

	@Override
	public boolean hasImpactResults() {
		return categoryIndex != null && !categoryIndex.isEmpty();
	}

	@SuppressWarnings("unchecked")
	void setCategoryIndex(Index<ImpactCategoryDescriptor> index) {
		this.categoryIndex = index;
		if (index != null)
			categoryResults = new List[index.size()];
	}

	void appendFlowResults(double[] vector) {
		appendResults(vector, flowResults);
	}

	void appendImpactResults(double[] vector) {
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

	public List<Double> getResults(Flow flow) {
		int idx = flowIndex.getIndex(flow);
		if (idx < 0)
			return Collections.emptyList();
		return flowResults[idx];
	}

	public List<Double> getResults(ImpactCategoryDescriptor impactCategory) {
		if (categoryIndex == null)
			return Collections.emptyList();
		int idx = categoryIndex.getIndex(impactCategory);
		if (idx < 0)
			return Collections.emptyList();
		return categoryResults[idx];
	}

	@Override
	public FlowDescriptor[] getFlows() {
		List<FlowDescriptor> descriptors = new ArrayList<>();
		for (Flow flow : flowIndex.getFlows())
			descriptors.add(Descriptors.toDescriptor(flow));
		return descriptors.toArray(new FlowDescriptor[descriptors.size()]);
	}

	@Override
	public ImpactCategoryDescriptor[] getImpactCategories() {
		if (categoryIndex == null)
			return new ImpactCategoryDescriptor[0];
		return categoryIndex.getItems();
	}

}
