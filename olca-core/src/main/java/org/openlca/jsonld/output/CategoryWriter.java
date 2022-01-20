package org.openlca.jsonld.output;

import org.openlca.core.model.Category;
import org.openlca.util.Categories;

import com.google.gson.JsonObject;

class CategoryWriter extends Writer<Category> {

	CategoryWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Category category) {
		JsonObject obj = super.write(category);
		if (obj == null)
			return null;
		Out.put(obj, "refId", Categories.createRefId(category));
		Out.put(obj, "modelType", category.modelType, Out.REQUIRED_FIELD);
		Out.put(obj, "category", category.category, conf);
		return obj;
	}
	
}

