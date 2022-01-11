package org.openlca.core.results.providers;

import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ResultFlow;
import org.openlca.core.model.ResultModel;
import org.openlca.core.model.descriptors.Descriptor;

/**
 * Provides a result provider view on a stored result model.
 */
public class ResultModelProvider implements ResultProvider {

	private final TechIndex techIndex;
	private final EnviIndex flowIndex;
	private final ImpactIndex impactIndex;
	private final double[] flowResults;
	private final double[] impactResults;

	public static ResultModelProvider of(ResultModel result) {
		return new ResultModelProvider(result);
	}

	private ResultModelProvider(ResultModel model) {
		var refFlow = TechFlow.of(model);
		techIndex = new TechIndex(refFlow);
		techIndex.setDemand(ReferenceAmount.get(model));

		// flow results
		flowIndex = flowIndexOf(model);
		if (flowIndex == null) {
			flowResults = null;
		} else {
			flowResults = new double[flowIndex.size()];
			for (var f : model.inventory) {
				if (isNonEnvi(f))
					continue;
				var idx = f.location == null
					? flowIndex.of(f.flow.id)
					: flowIndex.of(f.flow.id, f.location.id);
				if (idx < 0)
					continue;
				var amount = ReferenceAmount.get(f);
				if (amount == 0)
					continue;
				if (f.isInput) {
					amount = -amount;
				}
				flowResults[idx] += amount;
			}
		}

		// create the impact index and results
		if (model.impacts.isEmpty()) {
			impactIndex = null;
			impactResults = null;
		} else {
			impactIndex = new ImpactIndex();
			for (var imp : model.impacts) {
				impactIndex.add(Descriptor.of(imp.indicator));
			}
			impactResults = new double[impactIndex.size()];
			for (var imp : model.impacts) {
				if (imp.indicator == null)
					continue;
				var idx = impactIndex.of(imp.indicator.id);
				if (idx >= 0) {
					impactResults[idx] += imp.amount;
				}
			}
		}
	}

	private static EnviIndex flowIndexOf(ResultModel model) {
		if (model.inventory.isEmpty())
			return null;

		// fill the flow index
		boolean isRegionalized = false;
		for (var f : model.inventory) {
			if (isNonEnvi(f))
				continue;
			if (f.location != null) {
				isRegionalized = true;
				break;
			}
		}

		var flowIndex = isRegionalized
			? EnviIndex.createRegionalized()
			: EnviIndex.create();
		for (var f : model.inventory) {
			if (isNonEnvi(f))
				continue;
			flowIndex.add(EnviFlow.of(f));
		}

		return flowIndex.isEmpty() ? null : flowIndex;
	}

	private static boolean isNonEnvi(ResultFlow f) {
		return f == null
			|| f.flow == null
			|| f.flow.flowType != FlowType.ELEMENTARY_FLOW;
	}

	@Override
	public TechIndex techIndex() {
		return techIndex;
	}

	@Override
	public EnviIndex flowIndex() {
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
	public double[] techColumnOf(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		return new double[]{techIndex.getDemand()};
	}

	@Override
	public double[] solutionOfOne(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		var demand = techIndex.getDemand();
		return demand == 0
			? new double[]{0}
			: new double[]{1 / demand};
	}

	@Override
	public double loopFactorOf(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		return 1;
	}

	@Override
	public double[] unscaledFlowsOf(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		return flowResults;
	}

	@Override
	public double[] directFlowsOf(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		return flowResults;
	}

	@Override
	public double[] totalFlowsOfOne(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		var demand = techIndex.getDemand();
		return demand == 0
			? new double[flowIndex.size()]
			: scale(flowResults, 1 / demand);
	}

	@Override
	public double[] totalFlowsOf(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
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
	public double[] directImpactsOf(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		return impactResults;
	}

	@Override
	public double[] totalImpactsOfOne(int product) {
		if (product != 0)
			throw new IndexOutOfBoundsException(product);
		var demand = techIndex.getDemand();
		return demand == 0
			? new double[impactIndex.size()]
			: scale(impactResults, 1 / demand);
	}

	@Override
	public double[] totalImpacts() {
		return impactResults;
	}

	@Override
	public double directCostsOf(int product) {
		// TODO: not yet implemented
		return 0;
	}

	@Override
	public double totalCostsOfOne(int product) {
		// TODO: not yet implemented
		return 0;
	}

	@Override
	public double totalCosts() {
		// TODO: not yet implemented
		return 0;
	}
}
