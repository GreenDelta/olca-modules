package org.openlca.jsonld.output;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

class Uncertainties {

	static JsonObject map(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		var type = uncertainty.distributionType;
		if (type == null || type == UncertaintyType.NONE)
			return null;
		var obj = new JsonObject();
		Json.put(obj, "@type", Uncertainty.class.getSimpleName());
		Json.put(obj, "distributionType", type);
		switch (type) {
			case UNIFORM -> mapUniform(uncertainty, obj);
			case TRIANGLE -> mapTriangle(uncertainty, obj);
			case NORMAL -> mapNormal(uncertainty, obj);
			case LOG_NORMAL -> mapLogNormal(uncertainty, obj);
			default -> {
			}
		}
		return obj;
	}

	private static void mapUniform(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "minimum", uncertainty.parameter1);
		Json.put(obj, "maximum", uncertainty.parameter2);
	}

	private static void mapTriangle(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "minimum", uncertainty.parameter1);
		Json.put(obj, "mode", uncertainty.parameter2);
		Json.put(obj, "maximum", uncertainty.parameter3);
	}

	private static void mapNormal(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "mean", uncertainty.parameter1);
		Json.put(obj, "sd", uncertainty.parameter2);
	}

	private static void mapLogNormal(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "geomMean", uncertainty.parameter1);
		Json.put(obj, "geomSd", uncertainty.parameter2);
	}
}
