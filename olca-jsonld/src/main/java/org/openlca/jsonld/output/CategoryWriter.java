package org.openlca.jsonld.output;

import java.lang.reflect.Type;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.EntityStore;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class CategoryWriter implements Writer<Category> {

	private EntityStore store;

	public CategoryWriter() {
	}

	public CategoryWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(Category category) {
		if (category == null || store == null)
			return;
		if (store.contains(ModelType.CATEGORY, category.getRefId()))
			return;
		JsonObject obj = serialize(category, null, null);
		store.put(ModelType.CATEGORY, obj);
	}

	@Override
	public JsonObject serialize(Category category, Type type,
			JsonSerializationContext context) {
		JsonObject json = store == null ? new JsonObject() : store.initJson();
		map(category, json);
		return json;
	}

	private void map(Category category, JsonObject json) {
		JsonExport.addAttributes(category, json, store);
		ModelType modelType = category.getModelType();
		if (modelType != null)
			json.addProperty("modelType", modelType.name());
		JsonObject parentRef = Out.put(category.getParentCategory(), store);
		json.add("parentCategory", parentRef);
	}
}
