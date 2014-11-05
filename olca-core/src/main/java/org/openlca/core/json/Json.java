package org.openlca.core.json;

import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

public class Json {

	public static <T> T load(Class<T> clazz, String json) {
		return null;
	}

	public static String dump(Object obj) {
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		builder.registerTypeAdapter(Category.class, new CategorySerializer());
		Gson gson = builder.create();
		return gson.toJson(obj);
	}

	static void addContext(JsonObject object) {
		String url = "http://openlca.org/";
		JsonObject context = new JsonObject();
		context.addProperty("@vocab", url);
		JsonObject vocabType = new JsonObject();
		vocabType.addProperty("@type", "@vocab");
		context.add("modelType", vocabType);
		object.add("@context", context);
	}

	static void addAttributes(RootEntity entity, JsonObject object) {
		String type = entity.getClass().getSimpleName();
		object.addProperty("@type", type);
		object.addProperty("@id", entity.getRefId());
		object.addProperty("name", entity.getName());
		object.addProperty("description", entity.getDescription());
	}

}
