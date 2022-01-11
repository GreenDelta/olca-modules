package org.openlca.core.results.providers;

import org.openlca.core.math.ReferenceAmount;
import org.openlca.core.matrix.index.EnviFlow;
import org.openlca.core.matrix.index.EnviIndex;
import org.openlca.core.matrix.index.ImpactIndex;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ResultModel;

/**
 * Provides a result provider view on a stored result model.
 */
public class ResultModelProvider implements ResultProvider {

	private final ResultModel model;
	private final TechIndex techIndex;
	private final EnviIndex flowIndex;
	private final ImpactIndex impactIndex;

	private ResultModelProvider(ResultModel model) {
		this.model = model;
		var refFlow = TechFlow.of(model);
		techIndex = new TechIndex(refFlow);
		techIndex.setDemand(ReferenceAmount.get(model));

		// create the flow index
		if (model.inventory.isEmpty()) {
			flowIndex = null;
		} else {
			boolean isRegionalized = false;
			for (var f : model.inventory) {
				if (f.location != null) {
					isRegionalized = true;
					break;
				}
			}
			flowIndex = isRegionalized
				? EnviIndex.createRegionalized()
				: EnviIndex.create();
			for (var f : model.inventory) {
				flowIndex.add(EnviFlow)
			}
		}

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
}
