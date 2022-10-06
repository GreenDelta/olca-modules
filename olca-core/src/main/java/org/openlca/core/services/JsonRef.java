package org.openlca.core.services;

import com.google.gson.JsonObject;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ProcessType;
import org.openlca.core.model.descriptors.ActorDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.CurrencyDescriptor;
import org.openlca.core.model.descriptors.DQSystemDescriptor;
import org.openlca.core.model.descriptors.EpdDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.LocationDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
import org.openlca.core.model.descriptors.ResultDescriptor;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.SocialIndicatorDescriptor;
import org.openlca.core.model.descriptors.SourceDescriptor;
import org.openlca.core.model.descriptors.UnitGroupDescriptor;
import org.openlca.jsonld.Json;

/**
 * A utility class for reading information from a data set reference.
 */
public final class JsonRef {

	/**
	 * Reads the model type from the {@code @type} attribute of the given Json
	 * object. Returns {@code null} if the type cannot be identified.
	 */
	public static ModelType typeOf(JsonObject json) {
		if (json == null)
			return null;
		var s = Json.getString(json, "@type");
		if (s == null)
			return null;
		return switch (s) {
			case "Project" -> ModelType.PROJECT;
			case "ImpactMethod" -> ModelType.IMPACT_METHOD;
			case "ImpactCategory" -> ModelType.IMPACT_CATEGORY;
			case "ProductSystem" -> ModelType.PRODUCT_SYSTEM;
			case "Process" -> ModelType.PROCESS;
			case "Flow" -> ModelType.FLOW;
			case "FlowProperty" -> ModelType.FLOW_PROPERTY;
			case "UnitGroup" -> ModelType.UNIT_GROUP;
			case "Actor" -> ModelType.ACTOR;
			case "Source" -> ModelType.SOURCE;
			case "Category" -> ModelType.CATEGORY;
			case "Location" -> ModelType.LOCATION;
			case "SocialIndicator" -> ModelType.SOCIAL_INDICATOR;
			case "Currency" -> ModelType.CURRENCY;
			case "Parameter" -> ModelType.PARAMETER;
			case "DQSystem", "DqSystem" -> ModelType.DQ_SYSTEM;
			case "Result" -> ModelType.RESULT;
			case "Epd" -> ModelType.EPD;
			default -> null;
		};
	}

	/**
	 * Reads the ID from the {@code @id} attribute of the given Json object.
	 * Returns {@code null} if the ID is not defined.
	 */
	public static String idOf(JsonObject json) {
		return Json.getString(json, "@id");
	}

	public static RootDescriptor descriptorOf(JsonObject json) {
		var d = initDescriptor(json);
		if (d == null)
			return null;
		d.refId = idOf(json);
		d.name = Json.getString(json, "name");
		if (d instanceof FlowDescriptor f) {
			f.flowType = Json.getEnum(json, "flowType", FlowType.class);
		} else if (d instanceof ProcessDescriptor p) {
			p.processType = Json.getEnum(json, "processType", ProcessType.class);
			p.flowType = Json.getEnum(json, "flowType", FlowType.class);
		}
		return d;
	}

	private static RootDescriptor initDescriptor(JsonObject json) {
		var type = typeOf(json);
		if (type == null)
			return new RootDescriptor();
		return switch (type) {
			case PROJECT -> new ProjectDescriptor();
			case IMPACT_METHOD -> new ImpactMethodDescriptor();
			case IMPACT_CATEGORY -> new ImpactDescriptor();
			case PRODUCT_SYSTEM -> new ProductSystemDescriptor();
			case PROCESS -> new ProcessDescriptor();
			case FLOW -> new FlowDescriptor();
			case FLOW_PROPERTY -> new FlowPropertyDescriptor();
			case UNIT_GROUP -> new UnitGroupDescriptor();
			case ACTOR -> new ActorDescriptor();
			case SOURCE -> new SourceDescriptor();
			case CATEGORY -> new CategoryDescriptor();
			case LOCATION -> new LocationDescriptor();
			case SOCIAL_INDICATOR -> new SocialIndicatorDescriptor();
			case CURRENCY -> new CurrencyDescriptor();
			case PARAMETER -> new ParameterDescriptor();
			case DQ_SYSTEM -> new DQSystemDescriptor();
			case RESULT -> new ResultDescriptor();
			case EPD -> new EpdDescriptor();
		};
	}
}
