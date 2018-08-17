package org.openlca.core.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Represents the uncertainty distributions supported by openLCA. Three fields
 * are reserved for distribution parameters: <br>
 * <br>
 *
 * parameter 1:
 * <ul>
 * <li>Normal distribution: arithmetic mean value
 * <li>Lognormal distribution: geometric mean value
 * <li>Triangle distribution: min value
 * <li>Uniform distribution: min value
 * <li>None: mean / resulting amount
 * </ul>
 *
 * parameter 2:
 * <ul>
 * <li>Normal distribution: arithmetic standard deviation
 * <li>Lognormal distribution: geometric standard deviation
 * <li>Triangle distribution: most likely value (mode)
 * <li>Uniform distribution: max value
 * </ul>
 *
 * parameter 3:
 * <ul>
 * <li>Triangle distribution: max value
 * </ul>
 *
 * Each distribution parameter can take a value and additionally a formula.
 *
 */
@Embeddable
public class Uncertainty {

	@Column(name = "distribution_type")
	public UncertaintyType distributionType = UncertaintyType.NONE;

	@Column(name = "parameter1_value")
	public Double parameter1Value;

	@Column(name = "parameter1_formula")
	public String parameter1Formula;

	@Column(name = "parameter2_value")
	public Double parameter2Value;

	@Column(name = "parameter2_formula")
	public String parameter2Formula;

	@Column(name = "parameter3_value")
	public Double parameter3Value;

	@Column(name = "parameter3_formula")
	public String parameter3Formula;

	/**
	 * Creates default distribution with type set to NONE and the first
	 * parameter set with the given mean value.
	 */
	public static Uncertainty none(double mean) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.NONE;
		uncertainty.parameter1Value = mean;
		return uncertainty;
	}

	/**
	 * Creates a normal distribution.
	 *
	 * @param mean
	 *            the arithmetic mean.
	 * @param sd
	 *            the arithmetic standard deviation.
	 */
	public static Uncertainty normal(double mean, double sd) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.NORMAL;
		uncertainty.parameter1Value = mean;
		uncertainty.parameter2Value = sd;
		return uncertainty;
	}

	/**
	 * Creates a log-normal distribution.
	 *
	 * @param gmean
	 *            the geometric mean.
	 * @param gsd
	 *            the geometric standard deviation
	 */
	public static Uncertainty logNormal(double gmean, double gsd) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.LOG_NORMAL;
		uncertainty.parameter1Value = gmean;
		uncertainty.parameter2Value = gsd;
		return uncertainty;
	}

	/**
	 * Creates a uniform distribution.
	 *
	 * @param min
	 *            the minimum.
	 * @param max
	 *            the maximum.
	 */
	public static Uncertainty uniform(double min, double max) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.UNIFORM;
		uncertainty.parameter1Value = min;
		uncertainty.parameter2Value = max;
		return uncertainty;
	}

	/**
	 * Creates a triangle distribution.
	 *
	 * @param min
	 *            The minimum value.
	 * @param mode
	 *            The most likely value (the mode).
	 * @param max
	 *            The maximum value.
	 */
	public static Uncertainty triangle(double min, double mode, double max) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.TRIANGLE;
		uncertainty.parameter1Value = min;
		uncertainty.parameter2Value = mode;
		uncertainty.parameter3Value = max;
		return uncertainty;
	}

	@Override
	public Uncertainty clone() {
		Uncertainty clone = new Uncertainty();
		clone.distributionType = distributionType;
		clone.parameter1Formula = parameter1Formula;
		clone.parameter2Formula = parameter2Formula;
		clone.parameter3Formula = parameter3Formula;
		clone.parameter1Value = parameter1Value;
		clone.parameter2Value = parameter2Value;
		clone.parameter3Value = parameter3Value;
		return clone;
	}

	/**
	 * Scales the distribution parameters by the given factor. This multiplies
	 * every distribution parameter with the given factor. Except for the
	 * geometric standard deviation in log-normal distributions as this
	 * parameter is scale independent.
	 */
	public void scale(double factor) {
		if (parameter1Value != null)
			parameter1Value = factor * parameter1Value;
		if (parameter1Formula != null)
			parameter1Formula = factor + " * (" + parameter1Formula + ")";
		if (distributionType != UncertaintyType.LOG_NORMAL) {
			if (parameter2Value != null)
				parameter2Value = factor * parameter2Value;
			if (parameter2Formula != null)
				parameter2Formula = factor + " * (" + parameter2Formula + ")";
		}
		if (parameter3Value != null)
			parameter3Value = factor * parameter3Value;
		if (parameter3Formula != null)
			parameter3Formula = factor + " * (" + parameter3Formula + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(distributionType, parameter1Formula,
				parameter1Value, parameter2Formula, parameter2Value,
				parameter3Formula, parameter3Value);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null)
			return false;
		if (!Objects.equals(this.getClass(), obj.getClass()))
			return false;
		Uncertainty other = (Uncertainty) obj;
		if (this.distributionType != other.distributionType)
			return false;
		return Objects.equals(this.parameter1Value, other.parameter1Value)
				&& Objects.equals(this.parameter2Value, other.parameter2Value)
				&& Objects.equals(this.parameter3Value, other.parameter3Value)
				&& Objects.equals(this.parameter1Formula,
						other.parameter1Formula)
				&& Objects.equals(this.parameter2Formula,
						other.parameter2Formula)
				&& Objects.equals(this.parameter3Formula,
						other.parameter3Formula);
	}
}
