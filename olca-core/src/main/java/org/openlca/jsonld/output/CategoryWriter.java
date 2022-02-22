package org.openlca.jsonld.output;

import org.openlca.core.model.Category;
import org.openlca.jsonld.Json;
import org.openlca.util.Categories;

import com.google.gson.JsonObject;

class CategoryWriter extends Writer<Category> {

	CategoryWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	JsonObject write(Category category) {
		var obj = super.write(category);
		if (obj == null)
			return null;
		Json.put(obj, "@id", Categories.createRefId(category));
		Json.put(obj, "modelType", category.modelType);
		Json.put(obj, "category", exp.handleRef(category.category));
		return obj;
	}

}

