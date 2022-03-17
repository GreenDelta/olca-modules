package org.openlca.ipc.handlers;

import com.google.gson.JsonObject;
import org.openlca.core.database.EntityCache;
import org.openlca.core.model.Category;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Location;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.RootDescriptor;
import org.openlca.core.model.descriptors.CategoryDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.FlowPropertyDescriptor;
import org.openlca.core.model.descriptors.ImpactDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.jsonld.Enums;
import org.openlca.jsonld.Json;
import org.openlca.util.Categories;

import java.util.List;

final class JsonRef {

	private JsonRef() {
	}

	static JsonObject of(Descriptor d, EntityCache cache) {
		if (d == null)
			return null;
		JsonObject obj = new JsonObject();
		if (d.type != null) {
			String type = d.type.getModelClass().getSimpleName();
			Json.put(obj, "@type", type);
		}
		Json.put(obj, "@id", d.refId);
		Json.put(obj, "name", d.name);

		if (d instanceof RootDescriptor cd) {
			putCategoryPath(obj, cd, cache);
		}
		if (d instanceof CategoryDescriptor category) {
			putCategoryMetaData(obj, category);
		}
		if (d instanceof FlowDescriptor flow) {
			putFlowMetaData(obj, flow, cache);
		}
		if (d instanceof FlowPropertyDescriptor property) {
			putFlowPropertyMetaData(obj, property, cache);
		}
		if (d instanceof ProcessDescriptor process) {
			putProcessMetaData(obj, process, cache);
		}
		if (d instanceof ImpactDescriptor impact) {
			obj.addProperty("refUnit", impact.referenceUnit);
		}
		return obj;
	}

	private static void putCategoryPath(
		JsonObject ref, RootDescriptor d, EntityCache cache) {
		if (ref == null || d == null || cache == null
			|| d.category == null)
			return;
		Category cat = cache.get(Category.class, d.category);
		if (cat == null)
			return;
		List<String> path = Categories.path(cat);
		ref.addProperty("category", String.join("/", path));
	}

	private static void putCategoryMetaData(
		JsonObject ref, CategoryDescriptor d) {
		if (ref == null || d == null)
			return;
		if (d.categoryType != null) {
			String type = d.categoryType.getModelClass().getSimpleName();
			ref.addProperty("categoryType", type);
		}
	}

	private static void putFlowMetaData(
		JsonObject ref, FlowDescriptor d, EntityCache cache) {
		if (ref == null || d == null)
			return;
		if (d.flowType != null) {
			ref.addProperty("flowType", Enums.getLabel(d.flowType));
		}
		if (cache == null)
			return;
		if (d.location != null) {
			Location loc = cache.get(Location.class, d.location);
			if (loc != null) {
				ref.addProperty("location", loc.code);
			}
		}
		FlowProperty prop = cache.get(FlowProperty.class, d.refFlowPropertyId);
		if (prop != null && prop.unitGroup != null) {
			Unit unit = prop.unitGroup.referenceUnit;
			if (unit != null) {
				ref.addProperty("refUnit", unit.name);
			}
		}
	}

	private static void putFlowPropertyMetaData(
		JsonObject ref, FlowPropertyDescriptor d, EntityCache cache) {
		if (ref == null || d == null)
			return;
		if (cache == null)
			return;
		FlowProperty prop = cache.get(FlowProperty.class, d.id);
		if (prop != null && prop.unitGroup != null) {
			Unit unit = prop.unitGroup.referenceUnit;
			if (unit != null) {
				ref.addProperty("refUnit", unit.name);
			}
		}
	}

	private static void putProcessMetaData(
		JsonObject ref, ProcessDescriptor d, EntityCache cache) {
		if (ref == null || d == null)
			return;
		if (d.processType != null) {
			ref.addProperty("processType", Enums.getLabel(d.processType));
		}
		if (cache != null && d.location != null) {
			Location loc = cache.get(Location.class, d.location);
			if (loc != null) {
				ref.addProperty("location", loc.code);
			}
		}
	}


}
