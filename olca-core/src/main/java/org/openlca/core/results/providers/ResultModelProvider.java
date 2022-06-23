package org.openlca.core.results.providers;

import gnu.trove.list.array.TDoubleArrayList;
import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.Result;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * Provides a result provider view on a stored result model.
 */
public class ResultModelProvider implements ResultProvider {

	private final Demand demand;
	private final TechIndex techIndex;
	private final EnviIndex flowIndex;
	private final ImpactIndex impactIndex;
	private final double[] flowResults;
	private final double[] impactResults;

	public static ResultModelProvider of(Result result) {
		return new ResultModelProvider(result);
	}

	private ResultModelProvider(Result model) {
		var refFlow = TechFlow.of(model);
		demand = new Demand(refFlow, ReferenceAmount.get(model));
		techIndex = new TechIndex(refFlow);

		// inventory results
		var inventory = FlowResults.of(model);
		flowIndex = inventory.index;
		flowResults = inventory.results;

		// create the impact index and results
		if (model.impactResults.isEmpty()) {
			impactIndex = null;
			impactResults = EMPTY_VECTOR;
		} else {
			impactIndex = new ImpactIndex();
			for (var imp : model.impactResults) {
				impactIndex.add(Descriptor.of(imp.indicator));
			}
			impactResults = new double[impactIndex.size()];
			for (var imp : model.impactResults) {
				if (imp.indicator == null)
					continue;
				var idx = impactIndex.of(imp.indicator.id);
				if (idx >= 0) {
					impactResults[idx] += imp.amount;
				}
			}
		}
	}

	@Override
	public Demand demand() {
		return demand;
	}

	@Override
	public TechIndex techIndex() {
		return techIndex;
	}

	@Override
	public EnviIndex enviIndex() {
		return flowIndex;
	}

	@Override
	public ImpactIndex impactIndex() {
		return impactIndex;
	}

	@Override
	public boolean hasCosts() {
		// TODO: not yet supported in result models
		return false;
	}

	@Override
	public double[] scalingVector() {
		return new double[]{1};
	}

	@Override
	public double[] techColumnOf(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		return new double[]{demand.value()};
	}

	@Override
	public double[] solutionOfOne(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		var d = demand.value();
		return d == 0
			? new double[]{0}
			: new double[]{1 / d};
	}

	@Override
	public double loopFactorOf(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		return 1;
	}

	@Override
	public double[] unscaledFlowsOf(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		return flowResults;
	}

	@Override
	public double[] directFlowsOf(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		return flowResults;
	}

	@Override
	public double[] totalFlowsOfOne(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		var demand = demand().value();
		return demand == 0
			? new double[flowIndex.size()]
			: scale(flowResults, 1 / demand);
	}

	@Override
	public double[] totalFlowsOf(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		return flowResults;
	}

	@Override
	public double[] totalFlows() {
		return flowResults;
	}

	@Override
	public double[] impactFactorsOf(int flow) {
		// TODO: not yet implemented
		return new double[impactIndex.size()];
	}

	@Override
	public double[] directImpactsOf(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		return impactResults;
	}

	@Override
	public double[] totalImpactsOfOne(int techFlow) {
		if (techFlow != 0)
			throw new IndexOutOfBoundsException(techFlow);
		var d = demand.value();
		return d == 0
			? new double[impactIndex.size()]
			: scale(impactResults, 1 / d);
	}

	@Override
	public double[] totalImpacts() {
		return impactResults;
	}

	@Override
	public double directCostsOf(int techFlow) {
		// TODO: not yet implemented
		return 0;
	}

	@Override
	public double totalCostsOfOne(int techFlow) {
		// TODO: not yet implemented
		return 0;
	}

	@Override
	public double totalCosts() {
		// TODO: not yet implemented
		return 0;
	}

	private record FlowResults(EnviIndex index, double[] results) {

		static FlowResults of(Result model) {

			// determine the index characteristics
			boolean hasFlowResults = false;
			boolean isRegionalized = false;
			for (var f : model.flowResults) {
				if (isNonEnvi(f))
					continue;
				hasFlowResults = true;
				if (f.location != null) {
					isRegionalized = true;
					break;
				}
			}

			// no flow results => virtual impact flows
			if (!hasFlowResults) {
				var index = EnviIndex.create();
				var results = DoubleBuffer.withCapacity(model.impactResults.size());
				for (var impact : model.impactResults) {
					if (impact.indicator == null)
						continue;
					var indicator = Descriptor.of(impact.indicator);
					var virtualFlow = EnviFlow.virtualOf(indicator);
					var idx = index.add(virtualFlow);
					results.add(idx, impact.amount);
				}
				return index.isEmpty()
					? new FlowResults(null, EMPTY_VECTOR)
					: new FlowResults(index, results.toArray());
			}

			var flowIndex = isRegionalized
				? EnviIndex.createRegionalized()
				: EnviIndex.create();
			var results = DoubleBuffer.withCapacity(model.flowResults.size());
			for (var f : model.flowResults) {
				if (isNonEnvi(f))
					continue;
				int idx = flowIndex.add(EnviFlow.of(f));
				var amount = ReferenceAmount.get(f);
				if (amount != 0 && f.isInput) {
					amount = -amount;
				}
				results.add(idx, amount);
			}
			return new FlowResults(flowIndex, results.toArray());
		}

		private static boolean isNonEnvi(FlowResult f) {
			return f == null
				|| f.flow == null
				|| f.flow.flowType != FlowType.ELEMENTARY_FLOW;
		}
	}

	private record DoubleBuffer(TDoubleArrayList list) {

		static DoubleBuffer withCapacity(int n) {
			var list = new TDoubleArrayList(n);
			return new DoubleBuffer(list);
		}

		void add(int pos, double value) {
			while (list.size() <= pos) {
				list.add(0);
			}
			double current = list.getQuick(pos);
			list.setQuick(pos, current + value);
		}

		double[] toArray() {
			return list.toArray();
		}
	}

}
