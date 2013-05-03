package org.openlca.core.math;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.Expression;
import org.openlca.core.model.UncertaintyDistributionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A bucket of the simulation matrix that contains a number generator.
 */
class SimulationBucket {

	private NumberGenerator generator;
	private boolean input;

	public SimulationBucket(Exchange exchange) {
		input = exchange.isInput();
		UncertaintyDistributionType type = exchange.getDistributionType();
		if (type != null && type != UncertaintyDistributionType.NONE)
			generator = createGenerator(exchange);
		if (generator == null)
			generator = NumberGenerator.discrete(exchange.getConvertedResult());
	}

	public double nextValue() {
		return input ? -generator.next() : generator.next();
	}

	private NumberGenerator createGenerator(Exchange exchange) {
		double[] vals = null;
		NumberGenerator gen = null;
		switch (exchange.getDistributionType()) {
		case LOG_NORMAL:
			vals = fetchParameterValues(exchange, 2);
			if (vals != null)
				gen = NumberGenerator.logNormal(vals[0], vals[1]);
			break;
		case NORMAL:
			vals = fetchParameterValues(exchange, 2);
			if (vals != null)
				gen = NumberGenerator.normal(vals[0], vals[1]);
			break;
		case TRIANGLE:
			vals = fetchParameterValues(exchange, 3);
			if (vals != null)
				gen = NumberGenerator.triangular(vals[0], vals[2], vals[1]);
			break;
		case UNIFORM:
			vals = fetchParameterValues(exchange, 2);
			if (vals != null)
				gen = NumberGenerator.uniform(vals[0], vals[1]);
		default:
			break;
		}
		return gen;
	}

	private double[] fetchParameterValues(Exchange exchange, int params) {
		Expression[] expressions = { exchange.getUncertaintyParameter1(),
				exchange.getUncertaintyParameter2(),
				exchange.getUncertaintyParameter3() };
		double[] vals = new double[params];
		for (int i = 0; i < params; i++) {
			if (expressions[i] == null) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.warn("Could not create number generator for exchange {}",
						exchange);
				log.warn("parameter {} missing", i + 1);
				return null;
			}
			vals[i] = expressions[i].getValue();
		}
		return vals;
	}
}
