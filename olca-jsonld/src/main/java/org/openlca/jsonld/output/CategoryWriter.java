package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Category;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class CategoryWriter extends Writer<Category> {

	@Override
	JsonObject write(Category category, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(category, refFn);
		if (obj == null)
			return obj;
		Out.put(obj, "modelType", category.getModelType());
		Out.put(obj, "category", category.getCategory(), refFn);
		return obj;
	}
}
