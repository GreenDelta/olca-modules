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
	private UncertaintyType distributionType = UncertaintyType.NONE;

	@Column(name = "parameter1_value")
	private Double parameter1Value;

	@Column(name = "parameter1_formula")
	private String parameter1Formula;

	@Column(name = "parameter2_value")
	private Double parameter2Value;

	@Column(name = "parameter2_formula")
	private String parameter2Formula;

	@Column(name = "parameter3_value")
	private Double parameter3Value;

	@Column(name = "parameter3_formula")
	private String parameter3Formula;

	/**
	 * Creates default distribution with type set to NONE and the first
	 * parameter set with the given mean value.
	 */
	public static Uncertainty none(double mean) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.setDistributionType(UncertaintyType.NONE);
		uncertainty.setParameter1Value(mean);
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
		uncertainty.setDistributionType(UncertaintyType.NORMAL);
		uncertainty.setParameter1Value(mean);
		uncertainty.setParameter2Value(sd);
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
		uncertainty.setDistributionType(UncertaintyType.LOG_NORMAL);
		uncertainty.setParameter1Value(gmean);
		uncertainty.setParameter2Value(gsd);
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
		uncertainty.setDistributionType(UncertaintyType.UNIFORM);
		uncertainty.setParameter1Value(min);
		uncertainty.setParameter2Value(max);
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
		uncertainty.setDistributionType(UncertaintyType.TRIANGLE);
		uncertainty.setParameter1Value(min);
		uncertainty.setParameter2Value(mode);
		uncertainty.setParameter3Value(max);
		return uncertainty;
	}

	@Override
	public Uncertainty clone() {
		Uncertainty clone = new Uncertainty();
		clone.setDistributionType(getDistributionType());
		clone.setParameter1Formula(getParameter1Formula());
		clone.setParameter2Formula(getParameter2Formula());
		clone.setParameter3Formula(getParameter3Formula());
		clone.setParameter1Value(getParameter1Value());
		clone.setParameter2Value(getParameter2Value());
		clone.setParameter3Value(getParameter3Value());
		return clone;
	}

	public UncertaintyType getDistributionType() {
		return distributionType;
	}

	public void setDistributionType(UncertaintyType distributionType) {
		this.distributionType = distributionType;
	}

	public Double getParameter1Value() {
		return parameter1Value;
	}

	public void setParameter1Value(Double parameter1Value) {
		this.parameter1Value = parameter1Value;
	}

	public String getParameter1Formula() {
		return parameter1Formula;
	}

	public void setParameter1Formula(String parameter1Formula) {
		this.parameter1Formula = parameter1Formula;
	}

	public Double getParameter2Value() {
		return parameter2Value;
	}

	public void setParameter2Value(Double parameter2Value) {
		this.parameter2Value = parameter2Value;
	}

	public String getParameter2Formula() {
		return parameter2Formula;
	}

	public void setParameter2Formula(String parameter2Formula) {
		this.parameter2Formula = parameter2Formula;
	}

	public Double getParameter3Value() {
		return parameter3Value;
	}

	public void setParameter3Value(Double parameter3Value) {
		this.parameter3Value = parameter3Value;
	}

	public String getParameter3Formula() {
		return parameter3Formula;
	}

	public void setParameter3Formula(String parameter3Formula) {
		this.parameter3Formula = parameter3Formula;
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
		//@formatter:off
		return     Objects.equals(this.parameter1Value, other.parameter1Value)
				&& Objects.equals(this.parameter2Value, other.parameter2Value)
				&& Objects.equals(this.parameter3Value, other.parameter3Value)
				&& Objects.equals(this.parameter1Formula, other.parameter1Formula)
				&& Objects.equals(this.parameter2Formula, other.parameter2Formula)
				&& Objects.equals(this.parameter3Formula, other.parameter3Formula);
		//@formatter:on
	}
}
