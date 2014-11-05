package org.openlca.core.json;

import java.lang.reflect.Type;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class CategoryWriter implements JsonSerializer<Category> {

	private final JsonWriter writer;

	public CategoryWriter(JsonWriter writer) {
		this.writer = writer;
	}

	@Override
	public JsonElement serialize(Category category, Type type,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		writer.addContext(json);
		mapContent(category, json);
		return json;
	}

	private void mapContent(Category category, JsonObject json) {
		writer.addAttributes(category, json);
		mapAttributes(category, json);
		JsonArray childs = new JsonArray();
		for (Category child : category.getChildCategories()) {
			JsonObject childObject = new JsonObject();
			mapContent(child, childObject);
			childs.add(childObject);
		}
		if (childs.size() > 0)
			json.add("childCategories", childs);
	}

	private void mapAttributes(Category category, JsonObject json) {
		ModelType modelType = category.getModelType();
		if (modelType != null)
			json.addProperty("modelType", modelType.name());
		Category parent = category.getParentCategory();
		if (parent != null) {
			JsonObject parentObj = new JsonObject();
			parentObj.addProperty("@id", parent.getRefId());
			json.add("parentCategory", parentObj);
		}
	}

}
