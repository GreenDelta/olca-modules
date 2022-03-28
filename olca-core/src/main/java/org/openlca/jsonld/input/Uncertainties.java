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
			case UNIFORM -> mapUniform(json, u);
			case TRIANGLE -> mapTriangle(json, u);
			case NORMAL -> mapNormal(json, u);
			case LOG_NORMAL -> mapLogNormal(json, u);
			default -> {
			}
		}
		return u;
	}

	private static void mapUniform(JsonObject json, Uncertainty u) {
		u.parameter1 = of(json, "minimum");
		u.parameter2 = of(json, "maximum");
	}

	private static void mapTriangle(JsonObject json, Uncertainty u) {
		u.parameter1 = of(json, "minimum");
		u.parameter2 = of(json, "mode");
		u.parameter3 = of(json, "maximum");
	}

	private static void mapNormal(JsonObject json, Uncertainty u) {
		u.parameter1 = of(json, "mean");
		u.parameter2 = of(json, "sd");
	}

	private static void mapLogNormal(JsonObject json, Uncertainty u) {
		u.parameter1 = of(json, "geomMean");
		u.parameter2 = of(json, "geomSd");
	}

	private static Double of(JsonObject json, String field) {
		var v = Json.getDouble(json, field);
		return v.isPresent()
			? v.getAsDouble()
			: null;
	}

}
