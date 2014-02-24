package org.openlca.simapro.csv.model.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ElementaryFlowType {

	RESOURCE("Resource", "Raw materials", Arrays.asList(
			SubCompartment.RAW_MATERIAL_IN_GROUND,
			SubCompartment.RAW_MATERIAL_IN_GROUND,
			SubCompartment.RAW_MATERIAL_IN_WATER,
			SubCompartment.RAW_MATERIAL_LAND,
			SubCompartment.RAW_MATERIAL_BIOTIC)),

	EMISSION_TO_AIR("Emission to air", "Airborne emissions", Arrays.asList(
			SubCompartment.AIRBORNE_HIGH_POP, SubCompartment.AIRBORNE_INDOOR,
			SubCompartment.AIRBORNE_LOW_POP,
			SubCompartment.AIRBORNE_LOW_POP_LONG_TERM,
			SubCompartment.AIRBORNE_STATOSPHERE,
			SubCompartment.AIRBORNE_STATOSPHERE_TROPOSHERE)),

	EMISSION_TO_WATER("Emission to water", "Waterborne emissions", Arrays
			.asList(SubCompartment.WATERBORNE_FOSSILWATER,
					SubCompartment.WATERBORNE_GROUNDWATER,
					SubCompartment.WATERBORNE_GROUNDWATER_LONG_TERM,
					SubCompartment.WATERBORNE_LAKE,
					SubCompartment.WATERBORNE_OCEAN,
					SubCompartment.WATERBORNE_RIVER,
					SubCompartment.WATERBORNE_RIVER_LONG_TERM)),

	FINAL_WASTE("Final waste flow", "Final waste flows", Arrays
			.asList(SubCompartment.UNSPECIFIED)),

	EMISSION_TO_SOIL("Emission to soil", "Emissions to soil", Arrays.asList(
			SubCompartment.SOIL_AGRICULTURAL, SubCompartment.SOIL_FORESTRY,
			SubCompartment.SOIL_INDUSTRIAL, SubCompartment.SOIL_URBAN)),

	NON_MATERIAL_EMISSIONS("Non material emission", "Non material emissions",
			Arrays.asList(SubCompartment.UNSPECIFIED)),

	SOCIAL_ISSUE("Social issue", "Social issues", Arrays
			.asList(SubCompartment.UNSPECIFIED)),

	ECONOMIC_ISSUE("Economic issue", "Economic issues", Arrays
			.asList(SubCompartment.UNSPECIFIED));

	private String value;
	private String substance;
	private List<SubCompartment> subCompartments = new ArrayList<SubCompartment>();

	private ElementaryFlowType(String value, String substance,
			List<SubCompartment> subCompartments) {
		this.value = value;
		this.substance = substance;
		this.subCompartments = subCompartments;
	}

	public String getValue() {
		return value;
	}

	public String getSubstance() {
		return substance;
	}

	public List<SubCompartment> getSubCompartments() {
		return subCompartments;
	}

	public static ElementaryFlowType forValue(String value) {
		for (ElementaryFlowType type : values()) {
			if (type.getValue().equals(value))
				return type;
		}
		return null;
	}

}
