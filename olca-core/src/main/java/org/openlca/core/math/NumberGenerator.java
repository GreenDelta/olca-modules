package org.openlca.core.math;

import org.ojalgo.random.LogNormal;
import org.ojalgo.random.Normal;
import org.ojalgo.random.Uniform;

public abstract class NumberGenerator {

	public abstract double next();

	public static NumberGenerator normal(double mean, double standardDeviation) {
		return new NormalDist(mean, standardDeviation);
	}

	public static NumberGenerator logNormal(double geometricMean,
			double geometricStandardDeviation) {
		return new LogNormalDist(geometricMean, geometricStandardDeviation);
	}

	public static NumberGenerator uniform(double min, double max) {
		if (min == max)
			return new DiscreteDist(max);
		if (max < min)
			return new UniformDist(max, min);
		return new UniformDist(min, max);
	}

	public static NumberGenerator triangular(double min, double max, double mode) {
		return new TriangularDist(min, max, mode);
	}

	public static NumberGenerator discrete(double val) {
		return new DiscreteDist(val);
	}

	private static class NormalDist extends NumberGenerator {

		private Normal fun;

		NormalDist(double mean, double std) {
			fun = new Normal(mean, std);
		}

		@Override
		public double next() {
			return fun.doubleValue();
		}
	}

	private static class LogNormalDist extends NumberGenerator {

		private LogNormal fun;

		LogNormalDist(double geoMean, double geoStd) {
			double mean = Math.log(Math.abs(geoMean)); // TODO: change signs if
														// < 0?
			double std = Math.log(Math.abs(geoStd));
			fun = new LogNormal(mean, std);
		}

		@Override
		public double next() {
			return fun.doubleValue();
		}
	}

	private static class UniformDist extends NumberGenerator {

		private Uniform fun;

		UniformDist(double min, double max) {
			fun = new Uniform(min, max - min);
		}

		@Override
		public double next() {
			return fun.doubleValue();
		}
	}

	private static class TriangularDist extends NumberGenerator {

		private double min;
		private double max;
		private double mode;

		TriangularDist(double min, double max, double mode) {
			this.max = max;
			this.min = min;
			this.mode = mode;
		}

		/**
		 * see http://en.wikipedia.org/wiki/Triangular_distribution
		 */
		@Override
		public double next() {
			if (max == min)
				return mode;
			double u = Math.random();
			double fMode = (mode - min) / (max - min);
			if (u <= fMode)
				return min + Math.sqrt(u * (max - min) * (mode - min));
			return max - Math.sqrt((1 - u) * (max - min) * (max - mode));
		}
	}

	private static class DiscreteDist extends NumberGenerator {

		private double val;

		public DiscreteDist(double val) {
			this.val = val;
		}

		@Override
		public double next() {
			return val;
		}

	}

}
