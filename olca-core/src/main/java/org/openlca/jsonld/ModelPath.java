package org.openlca.jsonld;

import org.openlca.core.model.ModelType;
import org.openlca.commons.Strings;

import java.util.Locale;
import java.util.Optional;

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
			case ACTOR -> "actors";
			case CATEGORY -> "categories";
			case CURRENCY -> "currencies";
			case DQ_SYSTEM -> "dq_systems";
			case EPD -> "epds";
			case FLOW -> "flows";
			case FLOW_PROPERTY -> "flow_properties";
			case IMPACT_CATEGORY -> "lcia_categories";
			case IMPACT_METHOD -> "lcia_methods";
			case LOCATION -> "locations";
			case PARAMETER -> "parameters";
			case PROCESS -> "processes";
			case PRODUCT_SYSTEM -> "product_systems";
			case PROJECT -> "projects";
			case RESULT -> "results";
			case SOCIAL_INDICATOR -> "social_indicators";
			case SOURCE -> "sources";
			case UNIT_GROUP -> "unit_groups";
		};
	}

	public static Optional<ModelType> typeOf(String folder) {
		if (Strings.isBlank(folder))
			return Optional.empty();
		return switch (folder.strip().toLowerCase(Locale.US)) {
			case "actors" -> Optional.of(ModelType.ACTOR);
			case "categories" -> Optional.of(ModelType.CATEGORY);
			case "currencies" -> Optional.of(ModelType.CURRENCY);
			case "dq_systems" -> Optional.of(ModelType.DQ_SYSTEM);
			case "epds" -> Optional.of(ModelType.EPD);
			case "flows" -> Optional.of(ModelType.FLOW);
			case "flow_properties" -> Optional.of(ModelType.FLOW_PROPERTY);
			case "lcia_categories" -> Optional.of(ModelType.IMPACT_CATEGORY);
			case "lcia_methods" -> Optional.of(ModelType.IMPACT_METHOD);
			case "locations" -> Optional.of(ModelType.LOCATION);
			case "parameters" -> Optional.of(ModelType.PARAMETER);
			case "processes" -> Optional.of(ModelType.PROCESS);
			case "product_systems" -> Optional.of(ModelType.PRODUCT_SYSTEM);
			case "projects" -> Optional.of(ModelType.PROJECT);
			case "results" -> Optional.of(ModelType.RESULT);
			case "social_indicators" -> Optional.of(ModelType.SOCIAL_INDICATOR);
			case "sources" -> Optional.of(ModelType.SOURCE);
			case "unit_groups" -> Optional.of(ModelType.UNIT_GROUP);
			default -> Optional.empty();
		};
	}
}
