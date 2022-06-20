package org.openlca.core.matrix;

import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.model.CalculationSetup;

/**
 * The demand for which a result is calculated.
 *
 * @param techFlow the demanded product-output or waste-input
 * @param value    the amount of the demand given in the reference unit of the
 *                 product or waste flow
 */
public record Demand(TechFlow techFlow, double value) {

	public static Demand of(TechFlow techFlow, double value) {
		return new Demand(techFlow, value);
	}

	public static Demand of(CalculationSetup setup) {
		var process = setup.process();
		var flow = setup.flow();
		var techFlow = TechFlow.of(process, flow);
		return new Demand(techFlow, setup.demand());
	}

}
