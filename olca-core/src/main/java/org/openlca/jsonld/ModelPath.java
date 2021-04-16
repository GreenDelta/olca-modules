package org.openlca.jsonld;

import org.openlca.core.model.ModelType;

public final class ModelPath {

	private ModelPath() {
	}

	public static String getBin(ModelType type, String refId) {
		return "bin/" + ModelPath.get(type) + "/" + refId;
	}

	public static String get(ModelType type, String refId) {
		return ModelPath.get(type) + "/" + refId + ".json";
	}

	public static String get(ModelType type) {
		if (type == null)
			return "";
		return switch (type) {
			case CATEGORY -> "categories";
			case CURRENCY -> "currencies";
			case PROCESS -> "processes";
			case FLOW -> "flows";
			case FLOW_PROPERTY -> "flow_properties";
			case ACTOR -> "actors";
			case IMPACT_CATEGORY -> "lcia_categories";
			case IMPACT_METHOD -> "lcia_methods";
			case LOCATION -> "locations";
			case NW_SET -> "nw_sets";
			case PARAMETER -> "parameters";
			case PRODUCT_SYSTEM -> "product_systems";
			case PROJECT -> "projects";
			case SOCIAL_INDICATOR -> "social_indicators";
			case SOURCE -> "sources";
			case UNIT -> "units";
			case UNIT_GROUP -> "unit_groups";
			case DQ_SYSTEM -> "dq_systems";
			default -> "unknown";
		};
	}

}
