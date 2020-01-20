package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
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

	private HashMap<ProcessProduct, PinnedContributions> pinned = new HashMap<>();

	public SimulationResult(MatrixData data) {
		this.techIndex = data.techIndex;
		this.flowIndex = data.flowIndex;
		this.impactIndex = data.impactIndex;
	}

	/**
	 * Append the total LCI and LCIA result vectors of the given result to this
	 * simulation result.
	 */
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
	 * Append the given direct and upstream result of the pinned product to this
	 * result. We only append the respective vectors from the results. The
	 * indices of these vectors need to match with the indices of this result.
	 */
	public void append(ProcessProduct product,
			SimpleResult direct, SimpleResult upstream) {
		if (product == null || direct == null || upstream == null)
			return;
		PinnedContributions pc = pinned.get(product);
		if (pc == null) {
			pc = new PinnedContributions();
			pinned.put(product, pc);
		}
		if (direct.totalFlowResults != null) {
			pc.directLCI.add(direct.totalFlowResults);
		}
		if (direct.totalImpactResults != null) {
			pc.directLCIA.add(direct.totalImpactResults);
		}
		if (upstream.totalFlowResults != null) {
			pc.upstreamLCI.add(upstream.totalFlowResults);
		}
		if (upstream.totalImpactResults != null) {
			pc.upstreamLCIA.add(upstream.totalImpactResults);
		}
	}

	public Set<ProcessProduct> getPinnedProducts() {
		return pinned.keySet();
	}

	/**
	 * Get the result of the given flow in the iteration i (zero based).
	 */
	public double get(IndexFlow flow, int i) {
		if (flowIndex == null)
			return 0;
		int arrayIdx = flow.index;
		return adopt(flow, val(flowResults, i, arrayIdx));
	}

	/**
	 * Get the direct contribution of the given product to the given flow in the
	 * iteration i (zero based).
	 */
	public double getDirect(ProcessProduct product,
			IndexFlow flow, int i) {
		PinnedContributions pc = pinned.get(product);
		if (pc == null || flowIndex == null)
			return 0;
		int arrayIdx = flow.index;
		return adopt(flow, val(pc.directLCI, i, arrayIdx));
	}

	/**
	 * Get the direct contribution of the given product to the given flow for
	 * all iterations.
	 */
	public double[] getAllDirect(ProcessProduct product, IndexFlow flow) {
		int count = getNumberOfRuns();
		double[] vals = new double[count];
		for (int i = 0; i < count; i++) {
			vals[i] = getDirect(product, flow, i);
		}
		return vals;
	}

	/**
	 * Get the upstream contribution of the given product to the given flow in
	 * the iteration i (zero based).
	 */
	public double getUpstream(ProcessProduct product,
			IndexFlow flow, int i) {
		PinnedContributions pc = pinned.get(product);
		if (pc == null || flowIndex == null)
			return 0;
		int arrayIdx = flow.index;
		return adopt(flow, val(pc.upstreamLCI, i, arrayIdx));
	}

	/**
	 * Get the upstream contribution of the given product to the given flow for
	 * all iterations.
	 */
	public double[] getAllUpstream(ProcessProduct product,
			IndexFlow flow) {
		int count = getNumberOfRuns();
		double[] vals = new double[count];
		for (int i = 0; i < count; i++) {
			vals[i] = getUpstream(product, flow, i);
		}
		return vals;
	}

	/**
	 * Get all simulation results of the given flow.
	 */
	public double[] getAll(IndexFlow flow) {
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
	 * Get the direct contribution of the given product to the given LCIA
	 * category in the iteration i (zero based).
	 */
	public double getDirect(ProcessProduct product,
			ImpactCategoryDescriptor impact, int i) {
		PinnedContributions pc = pinned.get(product);
		if (pc == null || impactIndex == null)
			return 0;
		int arrayIdx = impactIndex.of(impact);
		return val(pc.directLCIA, i, arrayIdx);
	}

	/**
	 * Get the direct contribution of the given product to the given LCIA
	 * category for all iterations.
	 */
	public double[] getAllDirect(ProcessProduct product,
			ImpactCategoryDescriptor impact) {
		int count = getNumberOfRuns();
		double[] vals = new double[count];
		for (int i = 0; i < count; i++) {
			vals[i] = getDirect(product, impact, i);
		}
		return vals;
	}

	/**
	 * Get the upstream contribution of the given product to the given LCIA
	 * category in the iteration i (zero based).
	 */
	public double getUpstream(ProcessProduct product,
			ImpactCategoryDescriptor impact, int i) {
		PinnedContributions pc = pinned.get(product);
		if (pc == null || impactIndex == null)
			return 0;
		int arrayIdx = impactIndex.of(impact);
		return val(pc.upstreamLCIA, i, arrayIdx);
	}

	/**
	 * Get the upstream contribution of the given product to the given LCIA
	 * category for all iterations.
	 */
	public double[] getAllUpstream(ProcessProduct product,
			ImpactCategoryDescriptor impact) {
		int count = getNumberOfRuns();
		double[] vals = new double[count];
		for (int i = 0; i < count; i++) {
			vals[i] = getUpstream(product, impact, i);
		}
		return vals;
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

	private static double val(List<double[]> list, int listIdx, int arrayIdx) {
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

	public static class PinnedContributions {
		private List<double[]> directLCI = new ArrayList<>();
		private List<double[]> upstreamLCI = new ArrayList<>();
		private List<double[]> directLCIA = new ArrayList<>();
		private List<double[]> upstreamLCIA = new ArrayList<>();
	}
}
