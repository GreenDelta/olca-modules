package org.openlca.core.math.rand;

import java.util.concurrent.ThreadLocalRandom;

class Uniform implements NumberGenerator {

	private final double min;
	private final double range;

	Uniform(double min, double max) {
		this.min = min;
		this.range = max - min;
	}

	@Override
	public double next() {
		var rand = ThreadLocalRandom.current();
		return min + rand.nextDouble() * range;
	}
}
