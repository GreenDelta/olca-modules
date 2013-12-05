package org.openlca.io.ecospold2;

import org.openlca.core.model.Uncertainty;
import org.openlca.ecospold2.LogNormal;
import org.openlca.ecospold2.Normal;
import org.openlca.ecospold2.Triangular;
import org.openlca.ecospold2.Uniform;

public class UncertaintyConverter {

	public static Uncertainty toOpenLCA(org.openlca.ecospold2.Uncertainty spold) {
		if (spold == null)
			return null;
		if (spold.getLogNormal() != null)
			return toOpenLCA(spold.getLogNormal());
		else if (spold.getNormal() != null)
			return toOpenLCA(spold.getNormal());
		else if (spold.getTriangular() != null)
			return toOpenLCA(spold.getTriangular());
		else if (spold.getUniform() != null)
			return toOpenLCA(spold.getUniform());
		else
			return null;
	}

	public static org.openlca.ecospold2.Uncertainty fromOpenLCA(Uncertainty olca) {
		if (olca == null || olca.getDistributionType() == null)
			return null;
		org.openlca.ecospold2.Uncertainty uncertainty = new org.openlca.ecospold2.Uncertainty();
		switch (olca.getDistributionType()) {
		case LOG_NORMAL:
			uncertainty.setLogNormal(createLogNormal(olca));
			break;
		case NORMAL:
			uncertainty.setNormal(createNormal(olca));
			break;
		case TRIANGLE:
			uncertainty.setTriangular(createTriangular(olca));
			break;
		case UNIFORM:
			uncertainty.setUniform(createUniform(olca));
			break;
		default:
			break;
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
	public static Uncertainty toOpenLCA(LogNormal logNormal) {
		if (logNormal == null)
			return null;
		// a variance can be never smaller 0, but better we check this
		double v = logNormal.getVariance() < 0 ? 0 : logNormal.getVariance();
		double sigma = Math.sqrt(v);
		double gsd = Math.exp(sigma);
		return Uncertainty.logNormal(logNormal.getMeanValue(), gsd);
	}

	private static LogNormal createLogNormal(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		LogNormal logNormal = new LogNormal();
		if (uncertainty.getParameter1Value() != null)
			logNormal.setMeanValue(uncertainty.getParameter1Value());
		if (uncertainty.getParameter2Value() != null) {
			double gsd = uncertainty.getParameter2Value();
			double sigma = Math.log(gsd);
			double var = Math.pow(sigma, 2);
			logNormal.setVariance(var);
		}
		return logNormal;
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

	private static Normal createNormal(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		Normal normal = new Normal();
		if (uncertainty.getParameter1Value() != null)
			normal.setMeanValue(uncertainty.getParameter1Value());
		if (uncertainty.getParameter2Value() != null) {
			double sd = uncertainty.getParameter2Value();
			double var = Math.pow(sd, 2);
			normal.setVariance(var);
		}
		return normal;
	}

	public static Uncertainty toOpenLCA(Triangular triangular) {
		if (triangular == null)
			return null;
		return Uncertainty.triangle(triangular.getMinValue(),
				triangular.getMostLikelyValue(), triangular.getMaxValue());
	}

	private static Triangular createTriangular(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		Triangular triangular = new Triangular();
		if (uncertainty.getParameter1Value() != null)
			triangular.setMinValue(uncertainty.getParameter1Value());
		if (uncertainty.getParameter2Value() != null)
			triangular.setMostLikelyValue(uncertainty.getParameter2Value());
		if (uncertainty.getParameter3Value() != null)
			triangular.setMaxValue(uncertainty.getParameter3Value());
		return triangular;
	}

	public static Uncertainty toOpenLCA(Uniform uniform) {
		if (uniform == null)
			return null;
		return Uncertainty
				.uniform(uniform.getMinValue(), uniform.getMaxValue());
	}

	private static Uniform createUniform(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		Uniform uniform = new Uniform();
		if (uncertainty.getParameter1Value() != null)
			uniform.setMinValue(uncertainty.getParameter1Value());
		if (uncertainty.getParameter2Value() != null)
			uniform.setMaxValue(uncertainty.getParameter2Value());
		return uniform;
	}
}
