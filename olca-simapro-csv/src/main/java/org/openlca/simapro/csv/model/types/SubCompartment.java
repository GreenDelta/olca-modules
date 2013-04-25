package org.openlca.simapro.csv.model.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of possible sub compartments
 * 
 * @author Sebastian Greve
 * 
 */
public enum SubCompartment {

	/**
	 * 'High. pop.' sub compartment for airborne emission
	 */
	AIRBORNE_HIGH_POP("high. pop."),

	/**
	 * 'Indoor' sub compartment for airborne emission
	 */
	AIRBORNE_INDOOR("indoor"),

	/**
	 * 'Low. pop.' sub compartment for airborne emission
	 */
	AIRBORNE_LOW_POP("low. pop."),

	/**
	 * 'Low. pop., long-term' sub compartment for airborne emission
	 */
	AIRBORNE_LOW_POP_LONG_TERM("low. pop., long-term"),

	/**
	 * 'Stratosphere' sub compartment for airborne emission
	 */
	AIRBORNE_STATOSPHERE("stratosphere"),

	/**
	 * 'Stratosphere + troposhere' sub compartment for airborne emission
	 */
	AIRBORNE_STATOSPHERE_TROPOSHERE("stratosphere + troposhere"),

	/**
	 * 'Biotic' sub compartment for raw material
	 */
	RAW_MATERIAL_BIOTIC("biotic"),

	/**
	 * 'In air' sub compartment for raw material
	 */
	RAW_MATERIAL_IN_AIR("in air"),

	/**
	 * 'In ground' sub compartment for raw material
	 */
	RAW_MATERIAL_IN_GROUND("in ground"),

	/**
	 * 'In water' sub compartment for raw material
	 */
	RAW_MATERIAL_IN_WATER("in water"),

	/**
	 * 'Land' sub compartment for raw material
	 */
	RAW_MATERIAL_LAND("land"),

	/**
	 * 'Agricultural' sub compartment for soil emission
	 */
	SOIL_AGRICULTURAL("agricultural"),

	/**
	 * 'Forestry' sub compartment for soil emission
	 */
	SOIL_FORESTRY("forestry"),

	/**
	 * 'Industrial' sub compartment for soil emission
	 */
	SOIL_INDUSTRIAL("industrial"),

	/**
	 * 'Urban, non industrial' sub compartment for soil emission
	 */
	SOIL_URBAN("urban, non industrial"),

	/**
	 * 'Fossilwater' sub compartment for waterborne emission
	 */
	WATERBORNE_FOSSILWATER("fossilwater"),

	/**
	 * 'Groundwater' sub compartment for waterborne emission
	 */
	WATERBORNE_GROUNDWATER("groundwater"),

	/**
	 * 'Groundwater, long-term' sub compartment for waterborne emission
	 */
	WATERBORNE_GROUNDWATER_LONG_TERM("groundwater, long-term"),

	/**
	 * 'Lake' sub compartment for waterborne emission
	 */
	WATERBORNE_LAKE("lake"),

	/**
	 * 'Ocean' sub compartment for waterborne emission
	 */
	WATERBORNE_OCEAN("ocean"),

	/**
	 * 'River' sub compartment for waterborne emission
	 */
	WATERBORNE_RIVER("river"),

	/**
	 * 'River, long-term' sub compartment for waterborne emission
	 */
	WATERBORNE_RIVER_LONG_TERM("river, long-term"),

	/**
	 * unspecified
	 */
	UNSPECIFIED("unspecified");

	private String value;

	private SubCompartment(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SubCompartment forValue(String value) {
		SubCompartment compartment = null;
		int i = 0;
		while (compartment == null && i < values().length) {
			if (values()[i].getValue().equals(value)) {
				compartment = values()[i];
			} else {
				i++;
			}
		}
		return compartment;
	}

	private static final Map<String, SubCompartment> instances;

	static {
		Map<String, SubCompartment> lInstances = new HashMap<String, SubCompartment>();

		for (SubCompartment eft : SubCompartment.values()) {
			lInstances.put(eft.value, eft);
		}
		instances = Collections.unmodifiableMap(lInstances);
	}

	public static Map<String, SubCompartment> getInstances() {
		return instances;
	}

}
