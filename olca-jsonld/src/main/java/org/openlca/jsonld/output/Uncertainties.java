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
		Out.put(obj, "minimum", uncertainty.parameter1Value);
		Out.put(obj, "minimumFormula", uncertainty.parameter1Formula);
		Out.put(obj, "maximum", uncertainty.parameter2Value);
		Out.put(obj, "maximumFormula", uncertainty.parameter2Formula);
	}

	private static void mapTriangle(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "minimum", uncertainty.parameter1Value);
		Out.put(obj, "minimumFormula", uncertainty.parameter1Formula);
		Out.put(obj, "mode", uncertainty.parameter2Value);
		Out.put(obj, "modeFormula", uncertainty.parameter2Formula);
		Out.put(obj, "maximum", uncertainty.parameter3Value);
		Out.put(obj, "maximumFormula", uncertainty.parameter3Formula);
	}

	private static void mapNormal(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "mean", uncertainty.parameter1Value);
		Out.put(obj, "meanFormula", uncertainty.parameter1Formula);
		Out.put(obj, "sd", uncertainty.parameter2Value);
		Out.put(obj, "sdFormula", uncertainty.parameter2Formula);
	}

	private static void mapLogNormal(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "geomMean", uncertainty.parameter1Value);
		Out.put(obj, "geomMeanFormula", uncertainty.parameter1Formula);
		Out.put(obj, "geomSd", uncertainty.parameter2Value);
		Out.put(obj, "geomSdFormula", uncertainty.parameter2Formula);
	}
}
