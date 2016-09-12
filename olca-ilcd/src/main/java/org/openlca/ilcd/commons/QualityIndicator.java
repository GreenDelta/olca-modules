package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DataQualityIndicatorValues")
@XmlEnum
public enum QualityIndicator {

	/**
	 * Degree to which the data set reflects the true population of interest
	 * regarding technology, including for included background data sets, if
	 * any.
	 * 
	 */
	@XmlEnumValue("Technological representativeness")
	TECHNOLOGICAL_REPRESENTATIVENESS("Technological representativeness"),

	/**
	 * Degree to which the data set reflects the true population of interest
	 * regarding time / age of the data, including for included background data
	 * sets, if any.
	 * 
	 */
	@XmlEnumValue("Time representativeness")
	TIME_REPRESENTATIVENESS("Time representativeness"),

	/**
	 * Degree to which the data set reflects the true population of interest
	 * regarding geography such as e.g. country or site, including for included
	 * background data sets, if any.
	 * 
	 */
	@XmlEnumValue("Geographical representativeness")
	GEOGRAPHICAL_REPRESENTATIVENESS("Geographical representativeness"),

	/**
	 * Share of (elementary) flows that are quantitatively included in the
	 * inventory. Note that also the completeness of interim product and waste
	 * flows in the product model contributes to the overall completeness of the
	 * inventory.
	 * 
	 */
	@XmlEnumValue("Completeness")
	COMPLETENESS("Completeness"),

	/**
	 * Measure of the variability of the data values for each data expressed
	 * (e.g. low variance = high precision).
	 * 
	 */
	@XmlEnumValue("Precision")
	PRECISION("Precision"),

	/**
	 * The applied LCI methods and methodological choices (e.g. allocation,
	 * substitution, etc.) are in line with the goal and scope of the data set,
	 * especially its intended applications and decision support context (e.g.
	 * monitoring, product-specific decision support, strategic long-term
	 * decision support). The methods also have been consistently applied across
	 * all data including for included processes, if any.
	 * 
	 */
	@XmlEnumValue("Methodological appropriateness and consistency")
	METHODOLOGICAL_APPROPRIATENESS_AND_CONSISTENCY(
													"Methodological appropriateness and consistency"),

	/**
	 * The degree to which the data set's overall representativeness,
	 * completeness, precision as well as methodological appropriateness and
	 * consistency reflects the reality the data set is representing.
	 * 
	 */
	@XmlEnumValue("Overall quality")
	OVERALL_QUALITY("Overall quality");
	private final String value;

	QualityIndicator(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static QualityIndicator fromValue(String v) {
		for (QualityIndicator c : QualityIndicator.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
