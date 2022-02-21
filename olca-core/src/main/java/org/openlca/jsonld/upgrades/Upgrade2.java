package org.openlca.jsonld.upgrades;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;

import java.util.List;
import java.util.Objects;

class Upgrade2 extends Upgrade {

	private List<JsonObject> _rawImpactMethods;

	Upgrade2(JsonStoreReader reader) {
		super(reader);
	}

	@Override
	public int[] fromVersions() {
		return new int[]{0, 1};
	}

	@Override
	public int toVersion() {
		return 2;
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var object = super.get(type, refId);
		if (object == null)
			return null;
		if (type == ModelType.IMPACT_CATEGORY) {
			addImpactCategoryParams(object);
		}
		return object;
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
