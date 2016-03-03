package org.openlca.jsonld.output;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;

import com.google.gson.JsonObject;

class Uncertainties {

	static JsonObject map(Uncertainty uncertainty) {
		if (uncertainty == null)
			return null;
		UncertaintyType type = uncertainty.getDistributionType();
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
		Out.put(obj, "minimum", uncertainty.getParameter1Value());
		Out.put(obj, "minimumFormula", uncertainty.getParameter1Formula());
		Out.put(obj, "maximum", uncertainty.getParameter2Value());
		Out.put(obj, "maximumFormula", uncertainty.getParameter2Formula());
	}

	private static void mapTriangle(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "minimum", uncertainty.getParameter1Value());
		Out.put(obj, "minimumFormula", uncertainty.getParameter1Formula());
		Out.put(obj, "mode", uncertainty.getParameter2Value());
		Out.put(obj, "modeFormula", uncertainty.getParameter2Formula());
		Out.put(obj, "maximum", uncertainty.getParameter3Value());
		Out.put(obj, "maximumFormula", uncertainty.getParameter3Formula());
	}

	private static void mapNormal(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "mean", uncertainty.getParameter1Value());
		Out.put(obj, "meanFormula", uncertainty.getParameter1Formula());
		Out.put(obj, "sd", uncertainty.getParameter2Value());
		Out.put(obj, "sdFormula", uncertainty.getParameter2Formula());
	}

	private static void mapLogNormal(Uncertainty uncertainty, JsonObject obj) {
		Out.put(obj, "geomMean", uncertainty.getParameter1Value());
		Out.put(obj, "geomMeanFormula", uncertainty.getParameter1Formula());
		Out.put(obj, "geomSd", uncertainty.getParameter2Value());
		Out.put(obj, "geomSdFormula", uncertainty.getParameter2Formula());
	}
}
