package org.openlca.jsonld;

import org.openlca.core.model.ModelType;

public final class ModelPath {

	private ModelPath() {
	}

	/**
	 * Returns the folder where the linked binary files of the data set of the
	 * given type and ID are stored.
	 */
	public static String binFolderOf(ModelType type, String refId) {
		return "bin/" + ModelPath.folderOf(type) + "/" + refId;
	}

	/**
	 * Returns the full path of a Json file that contains a model of the given
	 * type and ID.
	 */
	public static String jsonOf(ModelType type, String refId) {
		return ModelPath.folderOf(type) + "/" + refId + ".json";
	}

	/**
	 * Returns the name of the folder that contains data sets of the given type.
	 */
	public static String folderOf(ModelType type) {
		if (type == null)
			return "unknown";
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
			case RESULT -> "results";
			case EPD -> "epds";
			case UNKNOWN -> "unknown";
		};
	}

}
