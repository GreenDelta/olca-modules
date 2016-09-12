
package org.openlca.ilcd.commons;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CompletenessTypeValues")
@XmlEnum
public enum ImpactCategory {

	/**
	 * Climate change / global warming
	 * 
	 */
	@XmlEnumValue("Climate change")
	CLIMATE_CHANGE("Climate change"),

	/**
	 * Stratospheric ozone layer depletion
	 * 
	 */
	@XmlEnumValue("Ozone depletion")
	OZONE_DEPLETION("Ozone depletion"),

	/**
	 * Photochemical oxidant creation / summer smog / tropospheric ozone
	 * formation
	 * 
	 */
	@XmlEnumValue("Summer smog")
	SUMMER_SMOG("Summer smog"),

	/**
	 * Eutrophication of land and water bodies
	 * 
	 */
	@XmlEnumValue("Eutrophication")
	EUTROPHICATION("Eutrophication"),

	/**
	 * Acidification of land and water bodies
	 * 
	 */
	@XmlEnumValue("Acidification")
	ACIDIFICATION("Acidification"),

	/**
	 * Human toxicity (EXcluding ionising radiation and respiratory inorganics)
	 * 
	 */
	@XmlEnumValue("Human toxicity")
	HUMAN_TOXICITY("Human toxicity"),

	/**
	 * Freshwater eco-toxicity (EXcluding ionising radiation)
	 * 
	 */
	@XmlEnumValue("Freshwater ecotoxicity")
	FRESHWATER_ECOTOXICITY("Freshwater ecotoxicity"),

	/**
	 * Seawater eco-toxicity (EXcluding ionising radiation)
	 * 
	 */
	@XmlEnumValue("Seawater eco-toxicity")
	SEAWATER_ECO_TOXICITY("Seawater eco-toxicity"),

	/**
	 * Terrestric eco-toxicity (EXcluding ionising radiation)
	 * 
	 */
	@XmlEnumValue("Terrestric eco-toxicity")
	TERRESTRIC_ECO_TOXICITY("Terrestric eco-toxicity"),

	/**
	 * Radioactivity / ionising radiation
	 * 
	 */
	@XmlEnumValue("Radioactivity")
	RADIOACTIVITY("Radioactivity"),

	/**
	 * Land use (occupation and transformation)
	 * 
	 */
	@XmlEnumValue("Land use")
	LAND_USE("Land use"),

	/**
	 * Non-renewable material resource depletion
	 * 
	 */
	@XmlEnumValue("Non-renewable material resource depletion")
	NON_RENEWABLE_MATERIAL_RESOURCE_DEPLETION("Non-renewable material resource depletion"),

	/**
	 * Renewable material resource consumption
	 * 
	 */
	@XmlEnumValue("Renewable material resource consumption")
	RENEWABLE_MATERIAL_RESOURCE_CONSUMPTION("Renewable material resource consumption"),

	/**
	 * Non-renewable primary energy depletion
	 * 
	 */
	@XmlEnumValue("Non-renewable primary energy depletion")
	NON_RENEWABLE_PRIMARY_ENERGY_DEPLETION("Non-renewable primary energy depletion"),

	/**
	 * Renewable primary energy consumption
	 * 
	 */
	@XmlEnumValue("Renewable primary energy consumption")
	RENEWABLE_PRIMARY_ENERGY_CONSUMPTION("Renewable primary energy consumption"),

	/**
	 * Particulate matter/respiratory inorganics
	 * 
	 */
	@XmlEnumValue("Particulate matter/respiratory inorganics")
	PARTICULATE_MATTER_RESPIRATORY_INORGANICS("Particulate matter/respiratory inorganics"),

	/**
	 * Depletion of gentic resources by consumption of specific animal and plant
	 * species
	 * 
	 */
	@XmlEnumValue("Species depletion")
	SPECIES_DEPLETION("Species depletion"),

	/**
	 * Noise
	 * 
	 */
	@XmlEnumValue("Noise")
	NOISE("Noise");

	private final String value;

	ImpactCategory(String v) {
		value = v;
	}

	public String value() {
		return value;
	}

	public static ImpactCategory fromValue(String v) {
		for (ImpactCategory c : ImpactCategory.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}
		throw new IllegalArgumentException(v);
	}

}
