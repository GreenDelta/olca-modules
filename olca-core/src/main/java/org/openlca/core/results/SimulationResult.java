package org.openlca.core.results;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openlca.core.matrix.IndexFlow;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.ProcessProduct;
import org.openlca.core.model.descriptors.ImpactDescriptor;

/**
 * The results of a Monte-Carlo-Simulation. The single result values of the
 * simulation runs are stored in an array of lists where the flow- and LCIA
 * category indices are mapped to the respective array rows and the result
 * values to the respective list entries.
 */
public class SimulationResult extends BaseResult {

	private final List<double[]> flowResults = new ArrayList<>();
	private final List<double[]> impactResults = new ArrayList<>();
	private final HashMap<ProcessProduct, PinnedContributions> pinned = new HashMap<>();

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
	 * Creates a new pinned contribution for the given product with which direct
	 * and upstream contributions of this product can be added to this result.
	 * We only append the respective vectors from these contributions. Thus, the
	 * indices of these vectors must match with the indices of this result.
	 */
	public PinnedContribution pin(ProcessProduct product) {
		return new PinnedContribution(this, product);
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
		int arrayIdx = flowIndex.of(flow);
		return adopt(flow, val(flowResults, i, arrayIdx));
	}

	/**
	 * Get the direct contribution of the given product to the given flow in the
	 * iteration i (zero based).
	 */
	public double getDirect(ProcessProduct product, IndexFlow flow, int i) {
		var pc = pinned.get(product);
		if (pc == null || flowIndex == null)
			return 0;
		int arrayIdx = flowIndex.of(flow);
		return adopt(flow, val(pc.directFlows, i, arrayIdx));
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
	public double getUpstream(
		ProcessProduct product, IndexFlow flow, int i) {
		PinnedContributions pc = pinned.get(product);
		if (pc == null || flowIndex == null)
			return 0;
		int arrayIdx = flowIndex.of(flow);
		return adopt(flow, val(pc.upstreamFlows, i, arrayIdx));
	}

	/**
	 * Get the upstream contribution of the given product to the given flow for
	 * all iterations.
	 */
	public double[] getAllUpstream(
		ProcessProduct product, IndexFlow flow) {
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
	public double get(ImpactDescriptor impact, int i) {
		if (impactIndex == null)
			return 0;
		int arrayIdx = impactIndex.of(impact);
		return val(impactResults, i, arrayIdx);
	}

	/**
	 * Get the direct contribution of the given product to the given LCIA
	 * category in the iteration i (zero based).
	 */
	public double getDirect(
		ProcessProduct product, ImpactDescriptor impact, int i) {
		var pc = pinned.get(product);
		if (pc == null || impactIndex == null)
			return 0;
		int arrayIdx = impactIndex.of(impact);
		return val(pc.directImpacts, i, arrayIdx);
	}

	/**
	 * Get the direct contribution of the given product to the given LCIA
	 * category for all iterations.
	 */
	public double[] getAllDirect(
		ProcessProduct product, ImpactDescriptor impact) {
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
	public double getUpstream(
		ProcessProduct product, ImpactDescriptor impact, int i) {
		var pc = pinned.get(product);
		if (pc == null || impactIndex == null)
			return 0;
		int arrayIdx = impactIndex.of(impact);
		return val(pc.upstreamImpacts, i, arrayIdx);
	}

	/**
	 * Get the upstream contribution of the given product to the given LCIA
	 * category for all iterations.
	 */
	public double[] getAllUpstream(ProcessProduct product,
			ImpactDescriptor impact) {
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
	public double[] getAll(ImpactDescriptor impact) {
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

	private static class PinnedContributions {
		private final List<double[]> directFlows = new ArrayList<>();
		private final List<double[]> upstreamFlows = new ArrayList<>();
		private final List<double[]> directImpacts = new ArrayList<>();
		private final List<double[]> upstreamImpacts = new ArrayList<>();
	}

	public static class PinnedContribution {

		private final SimulationResult result;
		private final ProcessProduct product;

		private double[] directFlows;
		private double[] upstreamFlows;
		private double[] directImpacts;
		private double[] upstreamImpacts;

		private PinnedContribution(
			SimulationResult result,
			ProcessProduct product) {
			this.result = result;
			this.product = product;
		}

		public PinnedContribution withDirectFlows(double[] v) {
			this.directFlows = v;
			return this;
		}

		public PinnedContribution withUpstreamFlows(double[] v) {
			this.upstreamFlows = v;
			return this;
		}

		public PinnedContribution withDirectImpacts(double[] v) {
			this.directImpacts = v;
			return this;
		}

		public PinnedContribution withUpstreamImpacts(double[] v) {
			this.upstreamImpacts = v;
			return this;
		}

		public void add() {
			var pinned = result.pinned.computeIfAbsent(
				product, p -> new PinnedContributions());
			if (directFlows != null) {
				pinned.directFlows.add(directFlows);
			}
			if (upstreamFlows != null) {
				pinned.upstreamFlows.add(upstreamFlows);
			}
			if (directImpacts != null) {
				pinned.directImpacts.add(directImpacts);
			}
			if (upstreamImpacts != null) {
				pinned.upstreamImpacts.add(upstreamImpacts);
			}
		}

	}
}
