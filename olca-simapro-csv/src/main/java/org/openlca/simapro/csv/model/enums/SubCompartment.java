package org.openlca.simapro.csv.model.enums;

public enum SubCompartment {

	AIRBORNE_HIGH_POP("high. pop."),

	AIRBORNE_INDOOR("indoor"),

	AIRBORNE_LOW_POP("low. pop."),

	AIRBORNE_LOW_POP_LONG_TERM("low. pop., long-term"),

	AIRBORNE_STATOSPHERE("stratosphere"),

	AIRBORNE_STATOSPHERE_TROPOSHERE("stratosphere + troposhere"),

	RAW_MATERIAL_BIOTIC("biotic"),

	RAW_MATERIAL_IN_AIR("in air"),

	RAW_MATERIAL_IN_GROUND("in ground"),

	RAW_MATERIAL_IN_WATER("in water"),

	RAW_MATERIAL_LAND("land"),

	SOIL_AGRICULTURAL("agricultural"),

	SOIL_FORESTRY("forestry"),

	SOIL_INDUSTRIAL("industrial"),

	SOIL_URBAN("urban, non industrial"),

	WATERBORNE_FOSSILWATER("fossilwater"),

	WATERBORNE_GROUNDWATER("groundwater"),

	WATERBORNE_GROUNDWATER_LONG_TERM("groundwater, long-term"),

	WATERBORNE_LAKE("lake"),

	WATERBORNE_OCEAN("ocean"),

	WATERBORNE_RIVER("river"),

	WATERBORNE_RIVER_LONG_TERM("river, long-term"),

	UNSPECIFIED("unspecified");

	private String value;

	private SubCompartment(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SubCompartment forValue(String value) {
		for (SubCompartment subCompartment : values())
			if (subCompartment.getValue().equals(value))
				return subCompartment;
		return UNSPECIFIED;
	}

}
