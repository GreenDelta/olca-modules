package org.openlca.jsonld.upgrades;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class Upgrade2 extends Upgrade {

	private List<JsonObject> _rawImpactMethods;
	private List<JsonObject> _rawNwSets;

	Upgrade2(JsonStoreReader reader) {
		super(reader);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var object = super.get(type, refId);
		if (object == null)
			return null;
		if (type == ModelType.IMPACT_METHOD) {
			inlineNwSets(object);
		}
		if (type == ModelType.IMPACT_CATEGORY) {
			addImpactCategoryParams(object);
		}
		return object;
	}

	private void inlineNwSets(JsonObject methodObj) {
		if (methodObj == null)
			return;

		var nwRefs = Json.getArray(methodObj, "nwSets");
		if (nwRefs == null || nwRefs.isEmpty())
			return;

		if (_rawNwSets == null) {
			_rawNwSets = super.getFiles("nw_sets").stream()
				.map(super::getJson)
				.filter(Objects::nonNull)
				.filter(JsonElement::isJsonObject)
				.map(JsonElement::getAsJsonObject)
				.toList();
		}

		var idx = new HashMap<String, JsonObject>();
		for (var nwSet : _rawNwSets) {
			var id = Json.getString(nwSet, "@id");
			if (id == null)
				continue;
			idx.put(id, nwSet);
		}
		if (idx.isEmpty())
			return;

		var nwSets = new JsonArray();
		Json.stream(nwRefs)
			.filter(JsonElement::isJsonObject)
			.map(ref -> Json.getString(ref.getAsJsonObject(), "@id"))
			.filter(Objects::nonNull)
			.map(idx::get)
			.filter(Objects::nonNull)
			.map(JsonObject::deepCopy)
			.forEach(nwSets::add);
		methodObj.add("nwSets", nwSets);
	}

	private void addImpactCategoryParams(JsonObject object) {
		var id = Json.getString(object, "@id");
		if (id == null)
			return;

		// check if there are already parameters
		var params = Json.getArray(object, "parameters");
		if (params != null)
			return;

		// copy possible method parameters into the impact category
		var methodObj = getMethodOfImpact(id);
		if (methodObj != null) {
			var methodParams = Json.getArray(methodObj, "parameters");
			var impactParams = new JsonArray();
			if (methodParams != null) {
				// TODO: maybe update the parameters
				Json.stream(methodParams)
					.filter(JsonElement::isJsonObject)
					.map(JsonElement::getAsJsonObject)
					.map(JsonObject::deepCopy)
					.forEach(impactParams::add);
			}
		}
	}

	private JsonObject getMethodOfImpact(String impactId) {

		if (_rawImpactMethods == null) {
			_rawImpactMethods = super.getRefIds(ModelType.IMPACT_METHOD)
				.stream()
				.map(methodId -> super.get(ModelType.IMPACT_METHOD, methodId))
				.filter(Objects::nonNull)
				.toList();
		}

		for (var methodObj : _rawImpactMethods) {
			var impRefs = Json.getArray(methodObj, "impactCategories");
			if (impRefs == null)
				continue;
			for (var impRef : impRefs) {
				if (!impRef.isJsonObject())
					continue;
				var impId = Json.getString(impRef.getAsJsonObject(), "@id");
				if (Objects.equals(impactId, impId))
					return methodObj;
			}
		}
		return null;
	}

}
