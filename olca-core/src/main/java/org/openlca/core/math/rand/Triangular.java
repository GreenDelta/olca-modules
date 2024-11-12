package org.openlca.core.math.rand;

import java.util.concurrent.ThreadLocalRandom;

record Triangular(
		double min, double mode, double max
) implements NumberGenerator {

	/// see http://en.wikipedia.org/wiki/Triangular_distribution
	@Override
	public double next() {
		if (max == min)
			return mode;
		double u = ThreadLocalRandom.current().nextDouble();
		double fMode = (mode - min) / (max - min);
		return u <= fMode
				? min + Math.sqrt(u * (max - min) * (mode - min))
				: max - Math.sqrt((1 - u) * (max - min) * (max - mode));
	}
}
