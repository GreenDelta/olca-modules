package org.openlca.core.results;

import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;

/**
 * The results of a Monte-Carlo-Simulation. The single result values of the
 * simulation runs are stored in an array of lists where the flow- and LCIA
 * category indices are mapped to the respective array rows and the result
 * values to the respective list entries.
 */
public class SimulationResult extends BaseResult {

	private final List<double[]> flowResults = new ArrayList<>();
	private final List<double[]> impactResults = new ArrayList<>();

	public void append(SimpleResult r) {
		if (r == null)
			return;
		if (r.totalFlowResults != null) {
			flowResults.add(r.totalFlowResults);
		}
		if (r.totalImpactResults != null) {
			impactResults.add(r.totalImpactResults);
		}
	}

	/**
	 * Get the result of the given flow in the iteration i (zero based).
	 */
	public double get(FlowDescriptor flow, int i) {
		if (flowIndex == null)
			return 0;
		int arrayIdx = flowIndex.of(flow);
		return adopt(flow, val(flowResults, i, arrayIdx));
	}

	/**
	 * Get all simulation results of the given flow.
	 */
	public double[] getAll(FlowDescriptor flow) {
		double[] vals = new double[flowResults.size()];
		for (int i = 0; i < flowResults.size(); i++) {
			vals[i] = get(flow, i);
		}
		return vals;
	}

	/**
	 * Get the result of the given LCIA category in the iteration i (zero
	 * based).
	 */
	public double get(ImpactCategoryDescriptor impact, int i) {
		if (impactIndex == null)
			return 0;
		int arrayIdx = impactIndex.of(impact);
		return val(impactResults, i, arrayIdx);
	}

	/**
	 * Get all simulation results of the given LCIA category.
	 */
	public double[] getAll(ImpactCategoryDescriptor impact) {
		double[] vals = new double[impactResults.size()];
		for (int i = 0; i < impactResults.size(); i++) {
			vals[i] = get(impact, i);
		}
		return vals;
	}

	public int getNumberOfRuns() {
		return flowResults.size();
	}

	private double val(List<double[]> list, int listIdx, int arrayIdx) {
		if (list == null
				|| listIdx < 0
				|| arrayIdx < 0
				|| list.size() <= listIdx)
			return 0;
		double[] vec = list.get(listIdx);
		if (vec == null || vec.length <= arrayIdx)
			return 0;
		return vec[arrayIdx];
	}

	// TODO: no LCC for Monte Carlo simulations ?
	@Override
	public boolean hasCostResults() {
		return false;
	}

}
