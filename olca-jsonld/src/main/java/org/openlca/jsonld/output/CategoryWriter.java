package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;

import com.google.gson.JsonObject;

class CategoryWriter extends Writer<Category> {

	@Override
	JsonObject write(Category category, Consumer<RootEntity> refHandler) {
		JsonObject obj = super.write(category, refHandler);
		if (obj == null)
			return obj;
		ModelType modelType = category.getModelType();
		if (modelType != null)
			obj.addProperty("modelType", modelType.name());
		JsonObject parentRef = createRef(category.getCategory(), refHandler);
		obj.add("category", parentRef);
		return obj;
	}
}
