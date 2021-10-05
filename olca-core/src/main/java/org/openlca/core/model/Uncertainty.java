package org.openlca.core.model;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.openlca.core.math.NumberGenerator;
import org.openlca.util.Strings;

/**
 * Represents the uncertainty distributions supported by openLCA. Three fields
 * are reserved for distribution parameters. See the documentation of the
 * respective fields to see which distribution parameters are stored in which
 * fields for the respective distribution types. Each distribution parameter can
 * take a value and additionally a formula.
 */
@Embeddable
public class Uncertainty implements Copyable<Uncertainty> {

	@Column(name = "distribution_type")
	public UncertaintyType distributionType = UncertaintyType.NONE;

	/**
	 * The first parameter of the uncertainty distribution:
	 * <ul>
	 * <li>Normal distribution: arithmetic mean value
	 * <li>Lognormal distribution: geometric mean value
	 * <li>Triangle distribution: min value
	 * <li>Uniform distribution: min value
	 * <li>None: mean / resulting amount
	 * </ul>
	 */
	@Column(name = "parameter1_value")
	public Double parameter1;

	/**
	 * A formula for the first distribution parameter {@link #parameter1}.
	 */
	@Column(name = "parameter1_formula")
	public String formula1;

	/**
	 * The second parameter of the uncertainty distribution:
	 * <ul>
	 * <li>Normal distribution: arithmetic standard deviation
	 * <li>Lognormal distribution: geometric standard deviation
	 * <li>Triangle distribution: most likely value (mode)
	 * <li>Uniform distribution: max value
	 * </ul>
	 */
	@Column(name = "parameter2_value")
	public Double parameter2;

	/**
	 * A formula for the second distribution parameter {@link #parameter2}.
	 */
	@Column(name = "parameter2_formula")
	public String formula2;

	/**
	 * The third parameter of the uncertainty distribution:
	 * <ul>
	 * <li>Triangle distribution: max value
	 * </ul>
	 */
	@Column(name = "parameter3_value")
	public Double parameter3;

	/**
	 * A formula for the third distribution parameter {@link #parameter3}.
	 */
	@Column(name = "parameter3_formula")
	public String formula3;

	/**
	 * Creates default distribution with type set to NONE and the first
	 * parameter set with the given mean value.
	 */
	public static Uncertainty none(double mean) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.NONE;
		uncertainty.parameter1 = mean;
		return uncertainty;
	}

	/**
	 * Creates a normal distribution.
	 *
	 * @param mean the arithmetic mean.
	 * @param sd   the arithmetic standard deviation.
	 */
	public static Uncertainty normal(double mean, double sd) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.NORMAL;
		uncertainty.parameter1 = mean;
		uncertainty.parameter2 = sd;
		return uncertainty;
	}

	/**
	 * Creates a log-normal distribution.
	 *
	 * @param gmean the geometric mean.
	 * @param gsd   the geometric standard deviation
	 */
	public static Uncertainty logNormal(double gmean, double gsd) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.LOG_NORMAL;
		uncertainty.parameter1 = gmean;
		uncertainty.parameter2 = gsd;
		return uncertainty;
	}

	/**
	 * Creates a uniform distribution.
	 *
	 * @param min the minimum.
	 * @param max the maximum.
	 */
	public static Uncertainty uniform(double min, double max) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.UNIFORM;
		uncertainty.parameter1 = min;
		uncertainty.parameter2 = max;
		return uncertainty;
	}

	/**
	 * Creates a triangle distribution.
	 *
	 * @param min  The minimum value.
	 * @param mode The most likely value (the mode).
	 * @param max  The maximum value.
	 */
	public static Uncertainty triangle(double min, double mode, double max) {
		Uncertainty uncertainty = new Uncertainty();
		uncertainty.distributionType = UncertaintyType.TRIANGLE;
		uncertainty.parameter1 = min;
		uncertainty.parameter2 = mode;
		uncertainty.parameter3 = max;
		return uncertainty;
	}

	@Override
	public Uncertainty copy() {
		Uncertainty clone = new Uncertainty();
		clone.distributionType = distributionType;
		clone.formula1 = formula1;
		clone.formula2 = formula2;
		clone.formula3 = formula3;
		clone.parameter1 = parameter1;
		clone.parameter2 = parameter2;
		clone.parameter3 = parameter3;
		return clone;
	}

	/**
	 * Scales the distribution parameters by the given factor. This multiplies
	 * every distribution parameter with the given factor. Except for the
	 * geometric standard deviation in log-normal distributions as this
	 * parameter is scale independent.
	 */
	public void scale(double factor) {
		if (parameter1 != null)
			parameter1 = factor * parameter1;
		if (formula1 != null)
			formula1 = factor + " * (" + formula1 + ")";
		if (distributionType != UncertaintyType.LOG_NORMAL) {
			if (parameter2 != null)
				parameter2 = factor * parameter2;
			if (formula2 != null)
				formula2 = factor + " * (" + formula2 + ")";
		}
		if (parameter3 != null)
			parameter3 = factor * parameter3;
		if (formula3 != null)
			formula3 = factor + " * (" + formula3 + ")";
	}

	@Override
	public int hashCode() {
		return Objects.hash(distributionType, formula1,
			parameter1, formula2, parameter2,
			formula3, parameter3);
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
		return Objects.equals(this.parameter1, other.parameter1)
					 && Objects.equals(this.parameter2, other.parameter2)
					 && Objects.equals(this.parameter3, other.parameter3)
					 && Objects.equals(this.formula1, other.formula1)
					 && Objects.equals(this.formula2, other.formula2)
					 && Objects.equals(this.formula3, other.formula3);
	}

	@Override
	public String toString() {
		if (distributionType == null)
			return "none";
		String template;
		switch (distributionType) {
			case LOG_NORMAL:
				template = "lognormal: gmean=%s gsigma=%s";
				return String.format(template, str(parameter1), str(parameter2));
			case NORMAL:
				template = "normal: mean=%s sigma=%s";
				return String.format(template, str(parameter1), str(parameter2));
			case UNIFORM:
				template = "uniform: min=%s max=%s";
				return String.format(template, str(parameter1), str(parameter2));
			case TRIANGLE:
				template = "triangular: min=%s mode=%s max=%s";
				return String.format(template, str(parameter1), str(parameter2),
					str(parameter3));
			default:
				return "none";
		}
	}

	/**
	 * A null-save method for getting the string representation of an
	 * uncertainty distribution.
	 */
	public static String string(Uncertainty u) {
		return u == null ? "none" : u.toString();
	}

	private String str(Double number) {
		if (number == null)
			return "0";
		return String.format(Locale.ENGLISH, "%.6G", number);
	}

	public static Uncertainty fromString(String s) {
		if (Strings.nullOrEmpty(s)) {
			Uncertainty u = new Uncertainty();
			u.distributionType = UncertaintyType.NONE;
			return u;
		}
		UncertaintyType[] types = {
			UncertaintyType.LOG_NORMAL,
			UncertaintyType.NORMAL,
			UncertaintyType.UNIFORM,
			UncertaintyType.TRIANGLE
		};
		String num = "([0-9,\\.,\\-,\\+,E,e]+)";
		String[] patterns = {
			"\\s*lognormal:\\s+gmean=" + num + "\\s+gsigma=" + num + "\\s*",
			"\\s*normal:\\s+mean=" + num + "\\s+sigma=" + num + "\\s*",
			"\\s*uniform:\\s+min=" + num + "\\s+max=" + num + "\\s*",
			"\\s*triangular:\\s+min=" + num + "\\s+mode=" + num + "\\s+max="
			+ num + "\\s*"
		};
		for (int i = 0; i < patterns.length; i++) {
			Pattern p = Pattern.compile(patterns[i]);
			Matcher m = p.matcher(s);
			if (!m.find())
				continue;
			switch (types[i]) {
				case LOG_NORMAL:
					return logNormal(d(m.group(1)), d(m.group(2)));
				case NORMAL:
					return normal(d(m.group(1)), d(m.group(2)));
				case UNIFORM:
					return uniform(d(m.group(1)), d(m.group(2)));
				case TRIANGLE:
					return triangle(d(m.group(1)), d(m.group(2)), d(m.group(3)));
				default:
			}
		}
		return null;
	}

	private static double d(String s) {
		if (Strings.nullOrEmpty(s))
			return 0;
		try {
			return Double.parseDouble(s);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Creates a number generator of this uncertainty distribution.
	 */
	public NumberGenerator generator() {
		double p1 = parameter1 != null ? parameter1 : 0;
		double p2 = parameter2 != null ? parameter2 : 0;
		if (distributionType == null)
			return NumberGenerator.discrete(p1);
		switch (distributionType) {
			case LOG_NORMAL:
				return NumberGenerator.logNormal(p1, p2);
			case NORMAL:
				return NumberGenerator.normal(p1, p2);
			case TRIANGLE:
				double p3 = parameter3 != null ? parameter3 : 0;
				return NumberGenerator.triangular(p1, p2, p3);
			case UNIFORM:
				return NumberGenerator.uniform(p1, p2);
			default:
				return NumberGenerator.discrete(p1);
		}
	}
}
