package org.openlca.jsonld.input;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

public final class Uncertainties {

	private Uncertainties() {
	}

	public static Uncertainty read(JsonObject json) {
		if (json == null)
			return null;
		UncertaintyType type = Json.getEnum(json, "distributionType",
				UncertaintyType.class);
		if (type == null)
			return null;
		Uncertainty u = new Uncertainty();
		u.distributionType = type;
		switch (type) {
		case UNIFORM:
			mapUniform(json, u);
			break;
		case TRIANGLE:
			mapTriangle(json, u);
			break;
		case NORMAL:
			mapNormal(json, u);
			break;
		case LOG_NORMAL:
			mapLogNormal(json, u);
			break;
		default:
			break;
		}
		return u;
	}

	private static void mapUniform(JsonObject json, Uncertainty u) {
		u.parameter1 = Json.getDouble(json, "minimum").orElse(null);
		u.parameter2 = Json.getDouble(json, "maximum").orElse(null);
	}

	private static void mapTriangle(JsonObject json, Uncertainty u) {
		u.parameter1 = Json.getDouble(json, "minimum").orElse(null);
		u.parameter2 = Json.getDouble(json, "mode").orElse(null);
		u.parameter3 = Json.getDouble(json, "maximum").orElse(null);
	}

	private static void mapNormal(JsonObject json, Uncertainty u) {
		u.parameter1 = Json.getDouble(json, "mean").orElse(null);
		u.parameter2 = Json.getDouble(json, "sd").orElse(null);
	}

	private static void mapLogNormal(JsonObject json, Uncertainty u) {
		u.parameter1 = Json.getDouble(json, "geomMean").orElse(null);
		u.parameter2 = Json.getDouble(json, "geomSd").orElse(null);
	}

}
