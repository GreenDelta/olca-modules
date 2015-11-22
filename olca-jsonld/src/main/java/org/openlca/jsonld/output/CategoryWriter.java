package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.RootEntity;
import org.openlca.jsonld.Enums;

import com.google.gson.JsonObject;

class CategoryWriter extends Writer<Category> {

	@Override
	JsonObject write(Category category, Consumer<RootEntity> refHandler) {
		JsonObject obj = super.write(category, refHandler);
		if (obj == null)
			return obj;
		obj.addProperty("modelType",
				Enums.getLabel(category.getModelType(), ModelType.class));
		JsonObject parentRef = References.create(category.getCategory(),
				refHandler);
		obj.add("category", parentRef);
		return obj;
	}
}
