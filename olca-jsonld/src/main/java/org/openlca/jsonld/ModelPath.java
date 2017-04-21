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
		switch (type) {
		case CATEGORY:
			return "categories";
		case CURRENCY:
			return "currencies";
		case PROCESS:
			return "processes";
		case FLOW:
			return "flows";
		case FLOW_PROPERTY:
			return "flow_properties";
		case ACTOR:
			return "actors";
		case IMPACT_CATEGORY:
			return "lcia_categories";
		case IMPACT_METHOD:
			return "lcia_methods";
		case LOCATION:
			return "locations";
		case NW_SET:
			return "nw_sets";
		case PARAMETER:
			return "parameters";
		case PRODUCT_SYSTEM:
			return "product_systems";
		case PROJECT:
			return "projects";
		case SOCIAL_INDICATOR:
			return "social_indicators";
		case SOURCE:
			return "sources";
		case UNIT:
			return "units";
		case UNIT_GROUP:
			return "unit_groups";
		case DQ_SYSTEM:
			return "dq_systems";
		default:
			return "unknown";
		}
	}

}
