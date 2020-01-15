package org.openlca.geo;

import org.openlca.core.matrix.AllocationIndex;
import org.openlca.core.matrix.InventoryConfig;
import org.openlca.core.matrix.RegFlowIndex;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.cache.FlowTable;
import org.openlca.core.matrix.format.MatrixBuilder;
import org.openlca.core.matrix.uncertainties.UMatrix;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.results.SimpleResult;

public class RegInventoryBuilder {

	private final InventoryConfig conf;
	private final TechIndex techIndex;
	private final FlowTable flows;

	private RegFlowIndex flowIndex;
	private AllocationIndex allocationIndex;

	private MatrixBuilder techBuilder;
	private MatrixBuilder enviBuilder;
	private UMatrix techUncerts;
	private UMatrix enviUncerts;
	private double[] costs;

	public RegInventoryBuilder(InventoryConfig conf) {
		this.conf = conf;
		this.techIndex = conf.techIndex;
		this.flows = FlowTable.create(conf.db);
		techBuilder = new MatrixBuilder();
		enviBuilder = new MatrixBuilder();
		if (conf.withUncertainties) {
			techUncerts = new UMatrix();
			enviUncerts = new UMatrix();
		}
		if (conf.withCosts) {
			costs = new double[conf.techIndex.size()];
		}
	}

	public RegMatrixData build() {
		if (conf.allocationMethod != null
				&& conf.allocationMethod != AllocationMethod.NONE) {
			allocationIndex = AllocationIndex.create(
					conf.db, techIndex, conf.allocationMethod);
		}

		// create the index of elementary flows; when the system has sub-systems
		// we add the flows of the sub-systems to the index; note that there
		// can be elementary flows that only occur in a sub-system
		flowIndex = new RegFlowIndex();
		if (conf.subResults != null) {
			for (SimpleResult sub : conf.subResults.values()) {
				if (sub.flowIndex == null)
					continue;
				sub.flowIndex.each((i, f) -> {
					if (!flowIndex.contains(f)) {
						if (sub.isInput(f)) {
							flowIndex.putInput(f);
						} else {
							flowIndex.putOutput(f);
						}
					}
				});
			}
		}

	}

}
