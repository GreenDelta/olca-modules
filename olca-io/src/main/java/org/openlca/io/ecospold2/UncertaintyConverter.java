package org.openlca.io.ecospold2;

import org.openlca.core.model.Uncertainty;
import org.openlca.ecospold2.LogNormal;
import org.openlca.ecospold2.Normal;
import org.openlca.ecospold2.Triangular;
import org.openlca.ecospold2.Uniform;

class UncertaintyConverter {

	public static Uncertainty toOpenLCA(org.openlca.ecospold2.Uncertainty spold) {
		if (spold == null)
			return null;
		if (spold.getLogNormal() != null)
			return toOpenLCA(spold.getLogNormal());
		else
			return null;
	}

	/**
	 * Converts the log-normal distribution from an EcoSpold 02 data set to a
	 * log-normal distribution in openLCA.
	 * 
	 * In openLCA the following distribution parameters are stored:
	 * <ol>
	 * <li>the geometric mean
	 * <li>the geometric standard deviation.
	 * </ol>
	 * 
	 * According to the EcoSpold 02 specification the log-normal distribution
	 * has the following attributes (among others):
	 * <ol>
	 * <li>meanValue: the geometric mean
	 * <li>variance: the unbiased variance of the underlying normal distribution
	 * </ol>
	 * 
	 * Thus, we convert the variance v of the underlying normal distribution to
	 * the geometric standard deviation gsd of the log-normal distribution (see
	 * http://en.wikipedia.org/wiki/Log-normal_distribution, geometric moments).
	 * 
	 * The standard deviation sigma of the underlying normal distribution is the
	 * square-root of the variance v and the geometric standard deviation gsd is
	 * the value of the exponential function for sigma: <code>
	 * 			sigma = sqrt(v) 
	 * 			gsd = exp(sigma)
	 * </code>
	 * 
	 */
	public static Uncertainty toOpenLCA(LogNormal logNormal) {
		if (logNormal == null)
			return null;
		// a variance can be never smaller 0, but better we check this
		double v = logNormal.getVariance() < 0 ? 0 : logNormal.getVariance();
		double sigma = Math.sqrt(v);
		double gsd = Math.exp(sigma);
		return Uncertainty.logNormal(logNormal.getMeanValue(), gsd);
	}

	public static Uncertainty toOpenLCA(Normal normal) {
		if (normal == null)
			return null;
		double mean = normal.getMeanValue();
		// a variance can be never smaller 0, but better we check this
		double v = normal.getVariance() < 0 ? 0 : normal.getVariance();
		double sd = Math.sqrt(v);
		return Uncertainty.normal(mean, sd);
	}

	public static Uncertainty toOpenLCA(Triangular triangular) {
		if (triangular == null)
			return null;
		return Uncertainty.triangle(triangular.getMinValue(),
				triangular.getMostLikelyValue(), triangular.getMaxValue());
	}

	public static Uncertainty toOpenLCA(Uniform uniform) {
		if (uniform == null)
			return null;
		return Uncertainty
				.uniform(uniform.getMinValue(), uniform.getMaxValue());
	}
}
