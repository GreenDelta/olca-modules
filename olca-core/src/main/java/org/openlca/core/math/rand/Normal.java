package org.openlca.core.math.rand;

import java.util.concurrent.ThreadLocalRandom;

record Normal(double mean, double std) implements NumberGenerator {

	@Override
	public double next() {
		var rand = ThreadLocalRandom.current().nextGaussian();
		return rand * std + mean;
	}
}
