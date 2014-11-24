package org.openlca.jsonld;

import java.lang.reflect.Type;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

class CategoryWriter implements JsonSerializer<Category> {

	@Override
	public JsonElement serialize(Category category, Type type,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		JsonWriter.addContext(json);
		map(category, json);
		return json;
	}

	private void map(Category category, JsonObject json) {
		JsonWriter.addAttributes(category, json);
		mapAttributes(category, json);
		JsonArray childs = new JsonArray();
		for (Category child : category.getChildCategories()) {
			JsonObject childObject = new JsonObject();
			map(child, childObject);
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
