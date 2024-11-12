package org.openlca.core.math.rand;

class LogNormal implements NumberGenerator {

	private final Normal normal;
	private final double factor;

	LogNormal(double geoMean, double geoStd) {

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

		// calculate the standard deviation and mean value of the underlying
		// normal distribution, this is just the natural logarithm of the
		// geometric standard deviation and geometric mean
		double sigma = Math.log(Math.abs(geoStd));
		double mu = Math.log(gmean);

		// the mean value of the logarithmic distribution is exp(mu + sigma^2/2)
		// in order to have the mean value of the resulting distribution of this
		// number generator to be like the expected value (geometric mean), we
		// reduce the mean of the underlying normal distribution by sigma^2/2
		normal = new Normal(mu - Math.pow(sigma, 2) / 2, sigma);
	}

	@Override
	public double next() {
		return Math.exp(normal.next()) * factor;
	}
}
