package org.openlca.core.matrices;

import org.openlca.core.math.NumberGenerator;
import org.openlca.core.model.UncertaintyDistributionType;

/**
 * A bucket of the simulation matrix that contains a number generator.
 */
class SimulationBucket {

	private NumberGenerator generator;
	private boolean input;

	public SimulationBucket(CalcExchange exchange) {
		input = exchange.isInput();
		UncertaintyDistributionType type = exchange.getUncertaintyType();
		if (type != null && type != UncertaintyDistributionType.NONE)
			generator = createGenerator(exchange);
		if (generator == null)
			generator = NumberGenerator.discrete(exchange.getAmount()
					* exchange.getConversionFactor());
	}

	public double nextValue() {
		return input ? -generator.next() : generator.next();
	}

	private NumberGenerator createGenerator(CalcExchange e) {
		switch (e.getUncertaintyType()) {
		case LOG_NORMAL:
			return NumberGenerator.logNormal(e.getParameter1(),
					e.getParameter2());
		case NORMAL:
			return NumberGenerator.normal(e.getParameter1(), e.getParameter2());
		case TRIANGLE:
			return NumberGenerator.triangular(e.getParameter1(),
					e.getParameter2(), e.getParameter3());
		case UNIFORM:
			return NumberGenerator
					.uniform(e.getParameter1(), e.getParameter2());
		default:
			return null;
		}
	}

}
