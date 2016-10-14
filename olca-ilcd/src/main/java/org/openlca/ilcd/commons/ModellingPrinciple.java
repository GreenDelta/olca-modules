
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "LCIMethodPrincipleValues")
@XmlEnum
public enum ModellingPrinciple {

	/**
	 * The process or product system is modeled in a strictly descriptive way
	 * using the employed specific or average technologies, market mixes, and
	 * time as occurring. This excludes the substitution of e.g. the burdens of
	 * co-products or of electricity produced from waste incineration and the
	 * modelling of consequences in the market. Data not related to or suitable
	 * for decision making but monitoring only. (Synonyms: descriptive, average,
	 * non-marginal, book-keeping.)
	 * 
	 */
	@XmlEnumValue("Attributional")
	ATTRIBUTIONAL("Attributional"),

	/**
	 * The process or product system is modeled considering the consequences
	 * it's production causes, assuming this additional demand results in
	 * additional production of the required materials, energy carriers,
	 * components etc. Typically the expected/assumed marginal change in the
	 * production of the named product, technology, market and time including
	 * for the background system. Depending on the specific consequential
	 * approach also other consequences in the economy's or global production
	 * system are considered. Substitution of co-products is one component often
	 * used in consequential modeling. Details are given in "Deviation from LCI
	 * method principle / explanations". Example: if a new product variant B
	 * requires more electricity in its use phase than the actual product A, it
	 * is assumed, that this additional electricity comes exclusively from
	 * natural gas combined-heat-and-power plants, as these are argued to be the
	 * actual marginal (i.e. additional) technology for electricity production
	 * installed to satisfy additional demand. Note that allocation is
	 * (generally) not appropriate under consequential modeling.
	 * 
	 */
	@XmlEnumValue("Consequential")
	CONSEQUENTIAL("Consequential"),

	/**
	 * The process or product system is in principal modeled considering the
	 * consequences it's production causes. However, in most cases e.g. due to
	 * rebound effects and the elasticity of the economy, the system is de-facto
	 * largely modelled as an attributional system, i.e. using allocation
	 * instead of system expansion for multifunctional processes. Details given
	 * in "Deviations from LCI method principle /explanations". [Note:
	 * Components that are often modeled in a consequential way are the
	 * recycling of materials and energy recovery. Also compare with the
	 * definitions of "Consequential" and "Attributional" modelling.]
	 * 
	 */
	@XmlEnumValue("Consequential with attributional components")
	CONSEQUENTIAL_WITH_ATTRIBUTIONAL_COMPONENTS("Consequential with attributional components"),

	/**
	 * Not applicable e.g. as modelled with an attributional approach but
	 * process without multifunctionality.
	 * 
	 */
	@XmlEnumValue("Not applicable")
	NOT_APPLICABLE("Not applicable"),

	/**
	 * Another LCI method principle was applied. To be detailed in "Deviation
	 * from LCI method principle / explanations".
	 * 
	 */
	@XmlEnumValue("Other")
	OTHER("Other");
	private final String value;

	ModellingPrinciple(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ModellingPrinciple fromValue(String v) {
		for (ModellingPrinciple c : ModellingPrinciple.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
