package org.openlca.jsonld.output;

import org.openlca.core.model.Category;

import com.google.gson.JsonObject;

class CategoryWriter extends Writer<Category> {

	CategoryWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(Category category) {
		JsonObject obj = super.write(category);
		if (obj == null)
			return obj;
		Out.put(obj, "modelType", category.getModelType(), Out.REQUIRED_FIELD);
		Out.put(obj, "category", category.getCategory(), conf);
		return obj;
	}
}
