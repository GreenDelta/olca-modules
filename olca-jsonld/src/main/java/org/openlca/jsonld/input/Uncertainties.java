package org.openlca.jsonld.input;

import org.openlca.core.model.Uncertainty;
import org.openlca.core.model.UncertaintyType;

import com.google.gson.JsonObject;

public final class Uncertainties {

	private Uncertainties() {
	}

	public static Uncertainty read(JsonObject json) {
		if (json == null)
			return null;
		UncertaintyType type = In.getEnum(json, "distributionType",
				UncertaintyType.class);
		if (type == null)
			return null;
		Uncertainty u = new Uncertainty();
		u.setDistributionType(type);
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
		u.setParameter1Value(In.getOptionalDouble(json, "minimum"));
		u.setParameter2Value(In.getOptionalDouble(json, "maximum"));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "minimumFormula"));
		// u.setParameter2Formula(In.getString(json, "maximumFormula"));
	}

	private static void mapTriangle(JsonObject json, Uncertainty u) {
		u.setParameter1Value(In.getOptionalDouble(json, "minimum"));
		u.setParameter2Value(In.getOptionalDouble(json, "mode"));
		u.setParameter3Value(In.getOptionalDouble(json, "maximum"));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "minimumFormula"));
		// u.setParameter2Formula(In.getString(json, "modeFormula"));
		// u.setParameter3Formula(In.getString(json, "maximumFormula"));
	}

	private static void mapNormal(JsonObject json, Uncertainty u) {
		u.setParameter1Value(In.getOptionalDouble(json, "mean"));
		u.setParameter2Value(In.getOptionalDouble(json, "sd"));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "meanFormula"));
		// u.setParameter2Formula(In.getString(json, "sdFormula"));
	}

	private static void mapLogNormal(JsonObject json, Uncertainty u) {
		u.setParameter1Value(In.getOptionalDouble(json, "geomMean"));
		u.setParameter2Value(In.getOptionalDouble(json, "geomSd"));
		// TODO: set formulas (when parameter import ready)
		// u.setParameter1Formula(In.getString(json, "geomMeanFormula"));
		// u.setParameter2Formula(In.getString(json, "geomSdFormula"));
	}

}
