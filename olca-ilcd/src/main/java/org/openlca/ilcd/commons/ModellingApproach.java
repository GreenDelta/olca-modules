
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "LCIMethodApproachesValues")
@XmlEnum
public enum ModellingApproach {

	/**
	 * Market-value based partitioning of the input and output flows of
	 * multi-functional processes to the function (i.e. good or service) of the
	 * product system under study.
	 * 
	 */
	@XmlEnumValue("Allocation - market value")
	ALLOCATION_MARKET_VALUE("Allocation - market value"),

	/**
	 * Gross calorific value based partitioning of the input and output flows of
	 * multi-functional processes to the good of the product system under study.
	 * 
	 */
	@XmlEnumValue("Allocation - gross calorific value")
	ALLOCATION_GROSS_CALORIFIC_VALUE("Allocation - gross calorific value"),

	/**
	 * Net (lower) calorific value based partitioning of the input and output
	 * flows of multi-functional processes to the good of the product system
	 * under study.
	 * 
	 */
	@XmlEnumValue("Allocation - net calorific value")
	ALLOCATION_NET_CALORIFIC_VALUE("Allocation - net calorific value"),

	/**
	 * Exergetic content based partitioning of the input and output flows of
	 * multi-functional processes to the good of the product system under study.
	 * 
	 */
	@XmlEnumValue("Allocation - exergetic content")
	ALLOCATION_EXERGETIC_CONTENT("Allocation - exergetic content"),

	/**
	 * Chemical element content based partitioning of the input and output flows
	 * of multi-functional processes to the good of the product system under
	 * study. One specific chemical element is chosen for allocation. Additional
	 * information is given in "Deviations from LCI method approaches /
	 * explanations".
	 * 
	 */
	@XmlEnumValue("Allocation - element content")
	ALLOCATION_ELEMENT_CONTENT("Allocation - element content"),

	/**
	 * Mass based partitioning of the input and output flows of multi-functional
	 * processes to the good of the product system under study.
	 * 
	 */
	@XmlEnumValue("Allocation - mass")
	ALLOCATION_MASS("Allocation - mass"),

	/**
	 * Volume based partitioning of the input and output flows of
	 * multi-functional processes to the good of the product system under study.
	 * 
	 */
	@XmlEnumValue("Allocation - volume")
	ALLOCATION_VOLUME("Allocation - volume"),

	/**
	 * Partitioning of the input and output flows of multi-functional processes
	 * to the function (i.e. good or service) of the product system under study,
	 * according to the assumed ability of that product to bear the
	 * environmental burden under market competitiveness conditions. Additional
	 * information is given in "Deviations from LCI method approaches /
	 * explanations". [Note: Also here the sum of burdens of all co-functions
	 * together is to be 100% of the total burden.]
	 * 
	 */
	@XmlEnumValue("Allocation - ability to bear")
	ALLOCATION_ABILITY_TO_BEAR("Allocation - ability to bear"),

	/**
	 * Partitioning of all INDIVIDUAL input or output flows of multi-functional
	 * processes to the function of the product system under study, according to
	 * the marginal causality that small changes of the relative amounts of the
	 * co-functions have on the amount of the respective individual input or
	 * output flow. Additional information/details are given in "Deviations from
	 * LCI method approaches / explanations". E.g. (illustrative, virtual
	 * example): Dioxin emissions of a mixed waste incineration process are
	 * allocated proportionally to the carbon and chlorine content of the
	 * different wastes, according to the marginal changes in Dioxin emissions
	 * that result from a marginal/small change in the carbon and chlorine
	 * composition of the incinerated mixed waste.
	 * 
	 */
	@XmlEnumValue("Allocation - marginal causality")
	ALLOCATION_MARGINAL_CAUSALITY("Allocation - marginal causality"),

	/**
	 * Partitioning of all INDIVIDUAL input or output flows of multi-functional
	 * processes to the function of the product system under study, according to
	 * the physical causality that the co-functions have on the amount of the
	 * respective individual input or output flow. Additional
	 * information/details are given in "Deviations from LCI method approaches /
	 * explanations". E.g. (illustrative, virtual example): all energy carrier
	 * input is allocated 100% to the only energy-containing co-product, while
	 * the non-energy containing, second Chromium-containing co-product receives
	 * 100% of the Chromium emissions and Chromium resource elementary flows.
	 * 
	 */
	@XmlEnumValue("Allocation - physical causality")
	ALLOCATION_PHYSICAL_CAUSALITY("Allocation - physical causality"),

	/**
	 * One user-defined and justified main function of the multi-functional
	 * processes is allocated 100% of all other input and output flows. Other
	 * co-functions are hence free of burden.
	 * 
	 */
	@XmlEnumValue("Allocation - 100% to main function")
	ALLOCATION_100_TO_MAIN_FUNCTION("Allocation - 100% to main function"),

	/**
	 * The individual input or output flows of multi-functional processes are
	 * assigned to the co-functions based on other criteria than those listed
	 * under the other "Allocation - ..." criteria. Additional information is
	 * given in "Deviations from LCI method approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Allocation - other explicit assignment")
	ALLOCATION_OTHER_EXPLICIT_ASSIGNMENT("Allocation - other explicit assignment"),

	/**
	 * All functions of a multi-functional process carry the same share of the
	 * other input or output flows. Independent of mass or other properties of
	 * products or educts, only the number of functions is considered (e.g.
	 * service output 1 receives 50% of the burdens, service 2 receives 50% of
	 * the burdens).
	 * 
	 */
	@XmlEnumValue("Allocation - equal distribution")
	ALLOCATION_EQUAL_DISTRIBUTION("Allocation - equal distribution"),

	/**
	 * Those function(s) of multi-functional processes that are not part of the
	 * product system under study are substituted by "Avoided product
	 * system(s)", i.e. by the inverted or negative LCI result of the respective
	 * function(s). The "Avoided product system" is modelled as "Best available
	 * technology" (BAT). Additional information on the used BAT technologies
	 * used is given in "Deviations from LCI method approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Substitution - BAT")
	SUBSTITUTION_BAT("Substitution - BAT"),

	/**
	 * Those function(s) of multi-functional processes that are not part of the
	 * product system under study are substituted by "Avoided product
	 * system(s)", i.e. by the inverted or negative LCI result of the respective
	 * function(s). The "Avoided product system" is modelled as average market
	 * mix of production, with a market price correction for different market
	 * values of the substituted product system. Additional information is given
	 * in "Deviations from LCI method approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Substitution - average, market price correction")
	SUBSTITUTION_AVERAGE_MARKET_PRICE_CORRECTION("Substitution - average, market price correction"),

	/**
	 * Those function(s) of multi-functional processes that are not part of the
	 * product system under study are substituted by "Avoided product
	 * system(s)", i.e. by the inverted or negative LCI result of the respective
	 * function(s). The "Avoided product system" is modelled as average market
	 * mix of production, with correction for different technical properties
	 * (e.g. fibre length, tensile stength, etc.) of the substituted product
	 * system. Additional information is given in "Deviations from LCI method
	 * approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Substitution - average, technical properties correction")
	SUBSTITUTION_AVERAGE_TECHNICAL_PROPERTIES_CORRECTION("Substitution - average, technical properties correction"),

	/**
	 * End-of-life products and wastes are cut-off in so far as recyclable
	 * materials and energy contents are handed over free of burden to
	 * subsequent uses/product systems, which however carry the burden of the
	 * recycling and related activities, transport etc. The amount of secondary
	 * materials and energy input into the first product system is modelled as
	 * is the content amount of these secondary resources in the product.
	 * Additional information is given in "Deviations from LCI method approaches
	 * / explanations".
	 * 
	 */
	@XmlEnumValue("Allocation - recycled content")
	ALLOCATION_RECYCLED_CONTENT("Allocation - recycled content"),

	/**
	 * End-of-life products and wastes are modelled to the secondary materials
	 * and recovered energy, which replace the respective primary production.
	 * The burden of the recycling and related activities, transport etc. are
	 * carried by the first product system. The substitution of primary
	 * production according to the first products recycling potential is
	 * typically corrected by the (lower) market price or technical quality of
	 * the secondary materials/energy carriers, if applicable. Additional
	 * information is given in "Deviations from LCI method approaches /
	 * explanations".
	 * 
	 */
	@XmlEnumValue("Substitution - recycling potential")
	SUBSTITUTION_RECYCLING_POTENTIAL("Substitution - recycling potential"),

	/**
	 * Those function(s) of multi-functional processes that are not part of the
	 * product system under study are substituted by "Avoided product
	 * system(s)", i.e. by the inverted or negative LCI result of the respective
	 * function(s). The "Avoided product system" is modelled as average market
	 * mix of production, without any correction factor of the substituted
	 * product system. Additional information is given in "Deviations from LCI
	 * method approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Substitution - average, no correction")
	SUBSTITUTION_AVERAGE_NO_CORRECTION("Substitution - average, no correction"),

	/**
	 * Those function(s) of multi-functional processes that are not part of the
	 * product system under study are substituted by "Avoided product
	 * system(s)", i.e. by the inverted or negative LCI result of the respective
	 * function(s). The "Avoided product system" is modelled as a specific
	 * product or process, that is found to be replaced. Additional information
	 * is given in "Deviations from LCI method approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Substitution - specific")
	SUBSTITUTION_SPECIFIC("Substitution - specific"),

	/**
	 * The Product system is modelled considering other or additional
	 * consequences in the economy, such as e.g. consumption or behavioral
	 * changes, or other effects even on other product systems that are not part
	 * of the one under study. Additional information is given in "Deviations
	 * from LCI method approaches / explanations".
	 * 
	 */
	@XmlEnumValue("Consequential effects - other")
	CONSEQUENTIAL_EFFECTS_OTHER("Consequential effects - other"),

	/**
	 * Process without multi-functionality or product system without any
	 * multi-functional process included.
	 * 
	 */
	@XmlEnumValue("Not applicable")
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Another LCI method approach is used. This is named in "Deviations from
	 * LCI method approaches / explanations" where also additional information
	 * is given.
	 * 
	 */
	@XmlEnumValue("Other")
	OTHER("Other");
	private final String value;

	ModellingApproach(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ModellingApproach fromValue(String v) {
		for (ModellingApproach c : ModellingApproach.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
