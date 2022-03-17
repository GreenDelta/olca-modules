package org.openlca.jsonld.output;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

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
		Json.put(obj, "minimumFormula", uncertainty.formula1);
		Json.put(obj, "maximum", uncertainty.parameter2);
		Json.put(obj, "maximumFormula", uncertainty.formula2);
	}

	private static void mapTriangle(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "minimum", uncertainty.parameter1);
		Json.put(obj, "minimumFormula", uncertainty.formula1);
		Json.put(obj, "mode", uncertainty.parameter2);
		Json.put(obj, "modeFormula", uncertainty.formula2);
		Json.put(obj, "maximum", uncertainty.parameter3);
		Json.put(obj, "maximumFormula", uncertainty.formula3);
	}

	private static void mapNormal(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "mean", uncertainty.parameter1);
		Json.put(obj, "meanFormula", uncertainty.formula1);
		Json.put(obj, "sd", uncertainty.parameter2);
		Json.put(obj, "sdFormula", uncertainty.formula2);
	}

	private static void mapLogNormal(Uncertainty uncertainty, JsonObject obj) {
		Json.put(obj, "geomMean", uncertainty.parameter1);
		Json.put(obj, "geomMeanFormula", uncertainty.formula1);
		Json.put(obj, "geomSd", uncertainty.parameter2);
		Json.put(obj, "geomSdFormula", uncertainty.formula2);
	}
}
