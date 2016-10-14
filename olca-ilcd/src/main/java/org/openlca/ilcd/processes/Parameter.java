
package org.openlca.ilcd.processes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.UncertaintyDistribution;
import org.openlca.ilcd.commons.annotations.Label;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VariableParameterType", propOrder = {
		"formula",
		"mean",
		"min",
		"max",
		"distribution",
		"dispersion",
		"comment",
		"other"
})
public class Parameter implements Serializable {

	private final static long serialVersionUID = 1L;

	/**
	 * Mathematical expression that determines the value of a variable. [Note: A
	 * parameter is defined by entering the value manually into the field "Mean
	 * value" and this field can be left empty.]
	 */
	@XmlElement(name = "formula")
	public String formula;

	/**
	 * Parameter value entered by user OR in case a formula is given in the
	 * "Formula" field, the result of the formula for the variable is displayed
	 * here.
	 */
	@XmlElement(name = "meanValue")
	public Double mean;

	/**
	 * Minimum value permissible for this parameter. For variables this field is
	 * empty.
	 */
	@XmlElement(name = "minimumValue")
	public Double min;

	/**
	 * Maximum value permissible for this parameter. For variables this field is
	 * empty.
	 */
	@XmlElement(name = "maximumValue")
	public Double max;

	/**
	 * Defines the kind of uncertainty distribution that is valid for this
	 * particular object or parameter.
	 */
	@XmlElement(name = "uncertaintyDistributionType")
	public UncertaintyDistribution distribution;

	/**
	 * The resulting overall uncertainty of the calculated variable value
	 * considering uncertainty of measurements, modelling, appropriateness etc.
	 * [Notes: For log-normal distribution the square of the geometric standard
	 * deviation (SDg^2) is stated. Mean value times SDg^2 equals the 97.5%
	 * value (= Maximum value), Mean value divided by SDg^2 equals the 2.5%
	 * value (= Minimum value). For normal distribution the doubled standard
	 * deviation value (2*SD) is entered. Mean value plus 2*SD equals 97.5%
	 * value (= Maximum value), Mean value minus 2*SD equals 2.5% value (=
	 * Minimum value). This data field remains empty when uniform or triangular
	 * uncertainty distribution is applied.]
	 */
	@XmlElement(name = "relativeStandardDeviation95In")
	public Double dispersion;

	/**
	 * Comment or description of variable or parameter. Typically including its
	 * unit and default values, e.g. in the pattern &lt;[unit] description;
	 * defaults; comments&gt;.
	 */
	@Label
	@XmlElement(name = "comment")
	public final List<LangString> comment = new ArrayList<>();

	@XmlElement(namespace = "http://lca.jrc.it/ILCD/Common")
	public Other other;

	/**
	 * Name of variable or parameter used as scaling factors for the "Mean
	 * amount" of individual inputs or outputs of the data set.
	 */
	@XmlAttribute(name = "name", required = true)
	public String name;

	@XmlAnyAttribute
	public final Map<QName, String> otherAttributes = new HashMap<>();

	@Override
	public Parameter clone() {
		Parameter clone = new Parameter();
		clone.formula = formula;
		clone.mean = mean;
		clone.min = min;
		clone.max = max;
		clone.distribution = distribution;
		clone.dispersion = dispersion;
		LangString.copy(comment, clone.comment);
		if (other != null)
			clone.other = other.clone();
		clone.name = name;
		clone.otherAttributes.putAll(otherAttributes);
		return clone;
	}
}
