package org.openlca.jsonld.output;

import java.lang.reflect.Type;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class UncertaintyWriter implements JsonSerializer<Uncertainty> {

	@Override
	public JsonElement serialize(Uncertainty uncertainty, Type type,
			JsonSerializationContext context) {
		JsonObject obj = new JsonObject();
		JsonWriter.addContext(obj);
		map(uncertainty, obj);
		return null;
	}

	static void map(Uncertainty uncertainty, JsonObject obj) {
		if (uncertainty == null || obj == null)
			return;
		UncertaintyType type = uncertainty.getDistributionType();
		if (type == null || type == UncertaintyType.NONE)
			return;
		obj.addProperty("@type", "Uncertainty");
		switch (type) {
		case UNIFORM:
			mapUniform(uncertainty, obj);
			break;
		case TRIANGLE:
			mapTriangle(uncertainty, obj);
			break;
		case NORMAL:
			mapNormal(uncertainty, obj);
			break;
		case LOG_NORMAL:
			mapLogNormal(uncertainty, obj);
			break;
		}
	}

	private static void mapUniform(Uncertainty uncertainty, JsonObject obj) {
		obj.addProperty("distributionType", "UNIFORM_DISTRIBUTION");
		obj.addProperty("minimum", uncertainty.getParameter1Value());
		obj.addProperty("minimumFormula", uncertainty.getParameter1Formula());
		obj.addProperty("maximum", uncertainty.getParameter2Value());
		obj.addProperty("maximumFormula", uncertainty.getParameter2Formula());
	}

	private static void mapTriangle(Uncertainty uncertainty, JsonObject obj) {
		obj.addProperty("distributionType", "TRIANGLE_DISTRIBUTION");
		obj.addProperty("minimum", uncertainty.getParameter1Value());
		obj.addProperty("minimumFormula", uncertainty.getParameter1Formula());
		obj.addProperty("mode", uncertainty.getParameter2Value());
		obj.addProperty("modeFormula", uncertainty.getParameter2Formula());
		obj.addProperty("maximum", uncertainty.getParameter3Value());
		obj.addProperty("maximumFormula", uncertainty.getParameter3Formula());
	}

	private static void mapNormal(Uncertainty uncertainty, JsonObject obj) {
		obj.addProperty("distributionType", "NORMAL_DISTRIBUTION");
		obj.addProperty("mean", uncertainty.getParameter1Value());
		obj.addProperty("meanFormula", uncertainty.getParameter1Formula());
		obj.addProperty("sd", uncertainty.getParameter2Value());
		obj.addProperty("sdFormula", uncertainty.getParameter2Formula());
	}

	private static void mapLogNormal(Uncertainty uncertainty, JsonObject obj) {
		obj.addProperty("distributionType", "LOG_NORMAL_DISTRIBUTION");
		obj.addProperty("geomMean", uncertainty.getParameter1Value());
		obj.addProperty("geomMeanFormula", uncertainty.getParameter1Formula());
		obj.addProperty("geomSd", uncertainty.getParameter2Value());
		obj.addProperty("geomSdFormula", uncertainty.getParameter2Formula());
	}
}
