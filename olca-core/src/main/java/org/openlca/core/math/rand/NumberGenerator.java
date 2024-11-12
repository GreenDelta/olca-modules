package org.openlca.core.math.rand;

public interface NumberGenerator {

	double next();

	/// Creates a number generator for a normal distribution with the given
	/// mean and standard deviation.
	static NumberGenerator normal(double mean, double sd) {
		return new Normal(mean, sd);
	}

	/// Creates a number generator for a log-normal distribution with the given
	/// geometric mean and geometric standard deviation.
	static NumberGenerator logNormal(double geoMean,double geoSd) {
		return new LogNormal(geoMean, geoSd);
	}

	/// Creates a number generator for a uniform distribution with a range between
	/// the given minimum and maximum value.
	static NumberGenerator uniform(double min, double max) {
		if (min == max)
			return new Discrete(max);
		if (max < min)
			return new Uniform(max, min);
		return new Uniform(min, max);
	}

	/// Creates a number generator for a triangular distribution with the given
	/// minimum, mode, and maximum value.
	static NumberGenerator triangular(double min, double mode, double max) {
		return new Triangular(min, mode, max);
	}

	/// Creates a "number generator" for a discrete value, this is just a
	// fall-back, which always returns the same value.
	static NumberGenerator discrete(double val) {
		return new Discrete(val);
	}
}
