package org.openlca.jsonld;

import java.lang.reflect.Type;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;


class CategoryWriter implements Writer<Category> {

	@Override
	public void write(Category category, EntityStore store) {
		if(store == null)
			return;
		Category cat = category;
		while(cat != null) {
			if(store.contains(ModelType.CATEGORY, category.getRefId()))
				break;
			JsonObject obj = serialize(cat, null, null);
			store.add(ModelType.CATEGORY, cat.getRefId(), obj);
			cat = cat.getParentCategory();
		}
	}

	@Override
	public JsonObject serialize(Category category, Type type,
			JsonSerializationContext context) {
		JsonObject json = new JsonObject();
		JsonWriter.addContext(json);
		map(category, json);
		return json;
	}

	private void map(Category category, JsonObject json) {
		JsonWriter.addAttributes(category, json);
		ModelType modelType = category.getModelType();
		if (modelType != null)
			json.addProperty("modelType", modelType.name());
		JsonObject parentRef = JsonWriter.createRef(category.getParentCategory());
		json.add("parentCategory", parentRef);
	}
}
