package org.openlca.jsonld.upgrades;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;
import org.openlca.util.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class Upgrade2 extends Upgrade {

	private List<JsonObject> _rawImpactMethods;
	private List<JsonObject> _rawNwSets;
	private final PathBuilder categories;

	Upgrade2(JsonStoreReader reader) {
		super(reader);
		categories = PathBuilder.of(reader);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var object = super.get(type, refId);
		if (object == null)
			return null;

		// replace category references with paths
		var categoryId = Json.getRefId(object, "category");
		if (categoryId != null) {
			var path = categories.getPath(categoryId);
			Json.put(object, "category", path);
		}

		if (type == ModelType.IMPACT_METHOD) {
			inlineNwSets(object);
		}
		if (type == ModelType.IMPACT_CATEGORY) {
			renameStr(object, "referenceUnitName", "refUnit");
			addImpactCategoryParams(object);
		}
		if (type == ModelType.PRODUCT_SYSTEM) {
			addRedefSets(object);
			renameObj(object, "referenceProcess", "refProcess");
			renameObj(object, "referenceExchange", "refExchange");
		}
		if (type == ModelType.PROCESS) {
			upgradeProcess(object);
		}
		if (type == ModelType.FLOW) {
			ugradeFlow(object);
		}
		if (type == ModelType.PARAMETER) {
			renameBool(object, "inputParameter", "isInputParameter");
		}
		if (type == ModelType.UNIT_GROUP) {
			upgradeUnitGroup(object);
		}
		if (type == ModelType.CURRENCY) {
			renameObj(object, "referenceCurrency", "refCurrency");
		}
		return object;
	}

	private void upgradeProcess(JsonObject object) {
		renameBool(object, "infrastructureProcess" ,"isInfrastructureProcess");
		var doc = Json.getObject(object, "processDocumentation");
		if (doc != null) {
			renameBool(doc, "copyright", "isCopyrightProtected");
		}
		var exchanges = Json.getArray(object, "exchanges");
		if (exchanges != null) {
			for (var elem : exchanges) {
				if (!elem.isJsonObject())
					continue;
				var exchange = elem.getAsJsonObject();
				renameBool(exchange, "avoidedProduct", "isAvoidedProduct");
				renameBool(exchange, "input", "isInput");
				renameBool(exchange, "quantitativeReference", "isQuantitativeReference");
			}
		}
	}

	private void ugradeFlow(JsonObject object) {
		renameBool(object, "infrastructureFlow" ,"isInfrastructureFlow");
		var props = Json.getArray(object, "flowProperties");
		if (props != null) {
			for (var e : props) {
				if (!e.isJsonObject())
					continue;
				var factor = e.getAsJsonObject();
				renameBool(factor, "referenceFlowProperty", "isRefFlowProperty");
			}
		}
	}

	private void upgradeUnitGroup(JsonObject object) {
		var units = Json.getArray(object, "units");
		if (units != null) {
			for (var e : units ) {
				if (!e.isJsonObject())
					continue;
				var u = e.getAsJsonObject();
				renameBool(u, "referenceUnit", "isRefUnit");
			}
		}
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
				Json.stream(methodParams)
					.filter(JsonElement::isJsonObject)
					.map(JsonElement::getAsJsonObject)
					.map(JsonObject::deepCopy)
					.forEach(param -> {
						renameBool(param, "inputParameter", "isIputParameter");
						impactParams.add(param);
					});
			}
			object.add("parameters", impactParams);
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

	private void addRedefSets(JsonObject systemObj) {

		// check of there are already paramater sets
		var currentSets = Json.getArray(systemObj, "parameterSets");
		if (currentSets != null)
			return;

		var params = Json.getArray(systemObj, "parameterRedefs");
		if (params == null)
			return;

		var set = new JsonObject();
		set.addProperty("name", "Baseline");
		set.addProperty("isBaseline", true);
		var redefs = new JsonArray();
		Json.stream(params)
			.filter(JsonElement::isJsonObject)
			.map(JsonElement::getAsJsonObject)
			.map(JsonObject::deepCopy)
			.forEach(redefs::add);
		set.add("parameters", redefs);

		var redefSets = new JsonArray();
		redefSets.add(set);
		systemObj.add("parameterSets", redefSets);
	}

	private void renameBool(JsonObject obj, String oldName, String newName) {
		var newVal = Json.getBool(obj, newName, false);
		if (newVal)
			return; // the new field is already set to true
		var val = Json.getBool(obj, oldName, false);
		Json.put(obj, newName, val);
	}

	private void renameStr(JsonObject obj, String oldName, String newName) {
		var newVal = Json.getString(obj, newName);
		if (Strings.notEmpty(newVal))
			return;
		var val = Json.getString(obj, oldName);
		Json.put(obj, newName, val);
	}

	private void renameObj(JsonObject obj, String oldName, String newName) {
		var newVal = Json.getObject(obj, newName);
		if (newVal != null)
			return;
		var val = Json.getObject(obj, oldName);
		if (val != null) {
			Json.put(obj, newName, val);
		}
	}

	private record PathBuilder(
		Map<String, String> names,
		Map<String, String> parents,
		Map<String, String> paths) {


		static PathBuilder of(JsonStoreReader reader) {
			var names = new HashMap<String, String>();
			var parents = new HashMap<String, String>();
			for (var file : reader.getFiles("categories")) {
				var json = reader.getJson(file);
				if (json == null || !json.isJsonObject())
					continue;
				var obj = json.getAsJsonObject();
				var id = Json.getString(obj, "@id");
				if (id == null)
					continue;
				var name = Json.getString(obj, "name");
				if (name != null) {
					names.put(id, name);
				}
				var parentId = Json.getRefId(obj, "category");
				if (parentId != null) {
					parents.put(id, parentId);
				}
			}
			var paths = new HashMap<String, String>();
			return new PathBuilder(names, parents, paths);
		}

		String getPath(String categoryId) {
			if (categoryId == null)
				return null;
			var cached = paths.get(categoryId);
			if (cached != null)
				return cached;
			var buffer = new StringBuilder();
			var nextId = categoryId;
			do {
				var name = names.get(nextId);
				if (buffer.length() > 0) {
					buffer.insert(0, name + "/");
				} else {
					buffer.append(name);
				}
				nextId = parents.get(nextId);
			} while (nextId != null);
			if (buffer.isEmpty())
				return null;
			var path = buffer.toString();
			paths.put(categoryId, path);
			return path;
		}
	}
}
