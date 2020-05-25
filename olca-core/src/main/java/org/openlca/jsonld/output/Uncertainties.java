package org.openlca.jsonld.output;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;

import com.google.gson.JsonObject;

class Uncertainties {

	static JsonObject map(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		UncertaintyType type = uncertainty.distributionType;
		if (type == null || type == UncertaintyType.NONE)
			return null;
		JsonObject obj = new JsonObject();
		Out.put(obj, "@type", Uncertainty.class.getSimpleName());
		Out.put(obj, "distributionType", type);
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
		default:
			break;
		}
		return obj;
	}

	private static void mapUniform(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "minimum", uncertainty.parameter1);
		Out.put(obj, "minimumFormula", uncertainty.formula1);
		Out.put(obj, "maximum", uncertainty.parameter2);
		Out.put(obj, "maximumFormula", uncertainty.formula2);
	}

	private static void mapTriangle(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "minimum", uncertainty.parameter1);
		Out.put(obj, "minimumFormula", uncertainty.formula1);
		Out.put(obj, "mode", uncertainty.parameter2);
		Out.put(obj, "modeFormula", uncertainty.formula2);
		Out.put(obj, "maximum", uncertainty.parameter3);
		Out.put(obj, "maximumFormula", uncertainty.formula3);
	}

	private static void mapNormal(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "mean", uncertainty.parameter1);
		Out.put(obj, "meanFormula", uncertainty.formula1);
		Out.put(obj, "sd", uncertainty.parameter2);
		Out.put(obj, "sdFormula", uncertainty.formula2);
	}

	private static void mapLogNormal(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "geomMean", uncertainty.parameter1);
		Out.put(obj, "geomMeanFormula", uncertainty.formula1);
		Out.put(obj, "geomSd", uncertainty.parameter2);
		Out.put(obj, "geomSdFormula", uncertainty.formula2);
	}
}
