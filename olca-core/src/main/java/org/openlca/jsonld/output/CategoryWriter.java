package org.openlca.jsonld.output;

import org.openlca.core.model.Category;
import org.openlca.jsonld.Json;
import org.openlca.util.Categories;

import com.google.gson.JsonObject;

record CategoryWriter(JsonExport exp) implements Writer<Category> {

	@Override
	public JsonObject write(Category category) {
		var obj = Writer.init(category);
		Json.put(obj, "@id", Categories.createRefId(category));
		Json.put(obj, "modelType", category.modelType);
		Json.put(obj, "category", exp.handleRef(category.category));
		return obj;
	}

}

