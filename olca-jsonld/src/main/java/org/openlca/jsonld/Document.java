package org.openlca.jsonld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.output.JsonExport;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Document implements EntityStore {

	private List<JsonObject> categories = new ArrayList<>();
	private List<JsonObject> locations = new ArrayList<>();
	private List<JsonObject> actors = new ArrayList<>();
	private List<JsonObject> sources = new ArrayList<>();
	private List<JsonObject> unitGroups = new ArrayList<>();
	private List<JsonObject> flowProperties = new ArrayList<>();
	private List<JsonObject> flows = new ArrayList<>();
	private List<JsonObject> processes = new ArrayList<>();
	private List<JsonObject> impactMethods = new ArrayList<>();
	private List<JsonObject> impactCategories = new ArrayList<>();
	private List<JsonObject> nwSets = new ArrayList<>();

	public static String toJson(RootEntity entity, IDatabase database) {
		Document document = new Document();
		JsonExport writer = new JsonExport(database, document);
		writer.write(entity);
		// TODO: add context to root object
		Gson gson = new Gson();
		return gson.toJson(document);
	}

	public static String toHtml(RootEntity entity, IDatabase database) {
		return new HtmlSerializer().serialize(entity, database);
	}

	@Override
	public void put(ModelType type, JsonObject object) {
		List<JsonObject> list = getList(type);
		if (list == null || object == null)
			return;
		list.add(object);
	}

	@Override
	public boolean contains(ModelType type, String refId) {
		List<JsonObject> list = getList(type);
		if (list == null)
			return false;
		for (JsonObject object : list) {
			String id = getId(object);
			if (Objects.equals(id, refId))
				return true;
		}
		return false;
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public JsonObject initJson() {
		return new JsonObject();
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		List<JsonObject> list = getList(type);
		if (list == null || refId == null)
			return null;
		for (JsonObject obj : list) {
			String id = getId(obj);
			if (Objects.equals(id, refId))
				return obj;
		}
		return null;
	}

	@Override
	public List<String> getRefIds(ModelType type) {
		List<JsonObject> list = getList(type);
		if (list == null)
			return Collections.emptyList();
		List<String> ids = new ArrayList<>();
		for (JsonObject obj : list) {
			String id = getId(obj);
			if (id != null)
				ids.add(id);
		}
		return ids;
	}

	private List<JsonObject> getList(ModelType type) {
		if (type == null)
			return null;
		switch (type) {
		case CATEGORY:
			return categories;
		case ACTOR:
			return actors;
		case SOURCE:
			return sources;
		case UNIT_GROUP:
			return unitGroups;
		case FLOW_PROPERTY:
			return flowProperties;
		case FLOW:
			return flows;
		case PROCESS:
			return processes;
		case LOCATION:
			return locations;
		case IMPACT_CATEGORY:
			return impactCategories;
		case IMPACT_METHOD:
			return impactMethods;
		case NW_SET:
			return nwSets;
		default:
			return null;
		}
	}

	private String getId(JsonObject obj) {
		if (obj == null)
			return null;
		JsonElement elem = obj.get("@id");
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
	}

}
