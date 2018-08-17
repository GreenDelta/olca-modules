package org.openlca.io.ecospold2;

import org.openlca.core.model.Uncertainty;

import spold2.LogNormal;
import spold2.Normal;
import spold2.Triangular;
import spold2.Uniform;

public class UncertaintyConverter {

	public static Uncertainty toOpenLCA(spold2.Uncertainty spold, double factor) {
		if (spold == null)
			return null;
		if (spold.logNormal != null)
			return toOpenLCA(spold.logNormal, factor);
		else if (spold.normal != null)
			return toOpenLCA(spold.normal, factor);
		else if (spold.triangular != null)
			return toOpenLCA(spold.triangular, factor);
		else if (spold.uniform != null)
			return toOpenLCA(spold.uniform, factor);
		else
			return null;
	}

	public static spold2.Uncertainty fromOpenLCA(Uncertainty olca) {
		if (olca == null || olca.distributionType == null)
			return null;
		spold2.Uncertainty uncertainty = new spold2.Uncertainty();
		switch (olca.distributionType) {
		case LOG_NORMAL:
			uncertainty.logNormal = createLogNormal(olca);
			break;
		case NORMAL:
			uncertainty.normal = createNormal(olca);
			break;
		case TRIANGLE:
			uncertainty.triangular = createTriangular(olca);
			break;
		case UNIFORM:
			uncertainty.uniform = createUniform(olca);
			break;
		case NONE:
			return null;
		default:
			return null;
		}
		return uncertainty;
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
	private static Uncertainty toOpenLCA(LogNormal logNormal, double factor) {
		if (logNormal == null)
			return null;
		// a variance can be never smaller 0, but better we check this
		double v = logNormal.variance < 0 ? 0 : logNormal.variance;
		double sigma = Math.sqrt(v);
		double gsd = Math.exp(sigma);
		return Uncertainty.logNormal(logNormal.meanValue * factor, gsd);
	}

	private static LogNormal createLogNormal(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		LogNormal logNormal = new LogNormal();
		if (uncertainty.parameter1 != null) {
			double gmean = uncertainty.parameter1;
			logNormal.meanValue = gmean;
			logNormal.mu = Math.log(gmean);
		}
		if (uncertainty.parameter2 != null) {
			double gsd = uncertainty.parameter2;
			double sigma = Math.log(gsd);
			double var = Math.pow(sigma, 2);
			logNormal.variance = var;
			logNormal.varianceWithPedigreeUncertainty = var;
		}
		return logNormal;
	}

	private static Uncertainty toOpenLCA(Normal normal, double factor) {
		if (normal == null)
			return null;
		double mean = normal.meanValue;
		// a variance can be never smaller 0, but better we check this
		double v = normal.variance < 0 ? 0 : normal.variance;
		double sd = Math.sqrt(v);
		return Uncertainty.normal(mean * factor, sd * factor);
	}

	private static Normal createNormal(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		Normal normal = new Normal();
		if (uncertainty.parameter1 != null)
			normal.meanValue = uncertainty.parameter1;
		if (uncertainty.parameter2 != null) {
			double sd = uncertainty.parameter2;
			double var = Math.pow(sd, 2);
			normal.variance = var;
		}
		return normal;
	}

	private static Uncertainty toOpenLCA(Triangular triangular, double factor) {
		if (triangular == null)
			return null;
		return Uncertainty.triangle(
				triangular.minValue * factor,
				triangular.mostLikelyValue * factor,
				triangular.maxValue * factor);
	}

	private static Triangular createTriangular(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		Triangular triangular = new Triangular();
		if (uncertainty.parameter1 != null)
			triangular.minValue = uncertainty.parameter1;
		if (uncertainty.parameter2 != null)
			triangular.mostLikelyValue = uncertainty.parameter2;
		if (uncertainty.parameter3 != null)
			triangular.maxValue = uncertainty.parameter3;
		return triangular;
	}

	private static Uncertainty toOpenLCA(Uniform uniform, double factor) {
		if (uniform == null)
			return null;
		return Uncertainty.uniform(
				uniform.minValue * factor,
				uniform.maxValue * factor);
	}

	private static Uniform createUniform(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		Uniform uniform = new Uniform();
		if (uncertainty.parameter1 != null)
			uniform.minValue = uncertainty.parameter1;
		if (uncertainty.parameter2 != null)
			uniform.maxValue = uncertainty.parameter2;
		return uniform;
	}
}
