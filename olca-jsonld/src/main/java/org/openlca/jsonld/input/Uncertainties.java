package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;

final class Uncertainties {

	private Uncertainties() {
	}

	static Uncertainty read(JsonObject json) {
		if (json == null)
			return null;
		String type = In.getString(json, "distributionType");
		if (type == null)
			return null;
		switch (type) {
		case "UNIFORM_DISTRIBUTION":
			return readUniform(json);
		case "TRIANGLE_DISTRIBUTION":
			return readTriangle(json);
		case "NORMAL_DISTRIBUTION":
			return readNormal(json);
		case "LOG_NORMAL_DISTRIBUTION":
			return readLogNormal(json);
		default:
			return null;
		}
	}

	private static Uncertainty readUniform(JsonObject json) {
		Uncertainty u = new Uncertainty();
		u.setDistributionType(UncertaintyType.UNIFORM);
		u.setParameter1Value(In.getDouble(json, "minimum", 0));
		u.setParameter2Value(In.getDouble(json, "maximum", 0));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "minimumFormula"));
		// u.setParameter2Formula(In.getString(json, "maximumFormula"));
		return u;
	}

	private static Uncertainty readTriangle(JsonObject json) {
		Uncertainty u = new Uncertainty();
		u.setDistributionType(UncertaintyType.TRIANGLE);
		u.setParameter1Value(In.getDouble(json, "minimum", 0));
		u.setParameter2Value(In.getDouble(json, "mode", 0));
		u.setParameter3Value(In.getDouble(json, "maximum", 0));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "minimumFormula"));
		// u.setParameter2Formula(In.getString(json, "modeFormula"));
		// u.setParameter3Formula(In.getString(json, "maximumFormula"));
		return u;
	}

	private static Uncertainty readNormal(JsonObject json) {
		Uncertainty u = new Uncertainty();
		u.setDistributionType(UncertaintyType.NORMAL);
		u.setParameter1Value(In.getDouble(json, "mean", 0));
		u.setParameter2Value(In.getDouble(json, "sd", 0));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "meanFormula"));
		// u.setParameter2Formula(In.getString(json, "sdFormula"));
		return u;
	}

	private static Uncertainty readLogNormal(JsonObject json) {
		Uncertainty u = new Uncertainty();
		u.setDistributionType(UncertaintyType.LOG_NORMAL);
		u.setParameter1Value(In.getDouble(json, "geomMean", 0));
		u.setParameter2Value(In.getDouble(json, "geomSd", 0));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "geomMeanFormula"));
		// u.setParameter2Formula(In.getString(json, "geomSdFormula"));
		return u;
	}

}
