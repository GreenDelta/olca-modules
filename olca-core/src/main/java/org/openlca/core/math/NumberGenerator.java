package org.openlca.core.math;

import java.util.concurrent.ThreadLocalRandom;

public abstract class NumberGenerator {

	public abstract double next();

	public static NumberGenerator normal(double mean, double standardDeviation) {
		return new Normal(mean, standardDeviation);
	}

	public static NumberGenerator logNormal(double geometricMean,
			double geometricStandardDeviation) {
		return new LogNormal(geometricMean, geometricStandardDeviation);
	}

	public static NumberGenerator uniform(double min, double max) {
		if (min == max)
			return new Discrete(max);
		if (max < min)
			return new Uniform(max, min);
		return new Uniform(min, max);
	}

	public static NumberGenerator triangular(double min, double mode, double max) {
		return new Triangular(min, mode, max);
	}

	public static NumberGenerator discrete(double val) {
		return new Discrete(val);
	}

	private static class Normal extends NumberGenerator {

		private final double mean;
		private final double std;

		Normal(double mean, double std) {
			this.mean = mean;
			this.std = std;
		}

		@Override
		public double next() {
			var rand = ThreadLocalRandom.current().nextGaussian();
			return rand * std + mean;
		}
	}

	private static class LogNormal extends NumberGenerator {

		private final Normal normal;
		private final double factor;

		LogNormal(double geoMean, double geoStd) {
			// the mean and the standard deviation of the *underlying*
			// distribution is the natural logarithm of the geometric mean and
			// geometric standard deviation
			// if the geometric mean is negative (ecoinvent 3!), we generate the
			// values first for the positive value and multiply them with -1
			double gmean;
			if (geoMean < 0) {
				gmean = Math.abs(geoMean);
				factor = -1;
			} else {
				gmean = geoMean;
				factor = 1;
			}
			double mean = Math.log(gmean);
			double std = Math.log(Math.abs(geoStd));
			normal = new Normal(mean, std);
		}

		@Override
		public double next() {
			return Math.exp(normal.next()) * factor;
		}
	}

	private static class Uniform extends NumberGenerator {

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

	private static class Triangular extends NumberGenerator {

		private final double min;
		private final double max;
		private final double mode;

		Triangular(double min, double mode, double max) {
			this.min = min;
			this.mode = mode;
			this.max = max;
		}

		/**
		 * see http://en.wikipedia.org/wiki/Triangular_distribution
		 */
		@Override
		public double next() {
			if (max == min)
				return mode;
			double u = ThreadLocalRandom.current().nextDouble();
			double fMode = (mode - min) / (max - min);
			if (u <= fMode)
				return min + Math.sqrt(u * (max - min) * (mode - min));
			return max - Math.sqrt((1 - u) * (max - min) * (max - mode));
		}
	}

	private static class Discrete extends NumberGenerator {

		private final double val;

		public Discrete(double val) {
			this.val = val;
		}

		@Override
		public double next() {
			return val;
		}
	}
}
