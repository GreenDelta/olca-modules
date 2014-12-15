package org.openlca.jsonld;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Document implements EntityStore {

	private List<JsonObject> categories = new ArrayList<>();
	private List<JsonObject> actors = new ArrayList<>();
	private List<JsonObject> sources = new ArrayList<>();
	private List<JsonObject> unitGroups = new ArrayList<>();
	private List<JsonObject> flowProperties = new ArrayList<>();
	private List<JsonObject> flows = new ArrayList<>();
	private List<JsonObject> processes = new ArrayList<>();
	private List<JsonObject> locations = new ArrayList<>();
	private List<JsonObject> impactCategories = new ArrayList<>();
	private List<JsonObject> impactMethods = new ArrayList<>();

	public static String toJson(RootEntity entity, IDatabase database) {
		Document document = new Document();
		JsonWriter writer = new JsonWriter(document);
		writer.write(entity, database);
		Gson gson = JsonWriter.createGson(WriterConfig.getDefault());
		return gson.toJson(document);
	}

	public static String toHtml(RootEntity entity, IDatabase database) {
		return new HtmlSerializer().serialize(entity, database);
	}

	@Override
	public void add(ModelType type, String refId, JsonObject object) {
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
			JsonElement id = object.get("@id");
			if (id == null)
				continue;
			if (Objects.equals(id.getAsString(), refId))
				return true;
		}
		return false;
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
		default:
			return null;
		}
	}

	@Override
	public void close() throws IOException {
	}
}
