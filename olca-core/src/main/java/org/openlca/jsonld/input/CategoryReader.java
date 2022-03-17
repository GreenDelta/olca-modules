package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Category;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

public record CategoryReader(EntityResolver resolver)
	implements EntityReader<Category> {

	public CategoryReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Category read(JsonObject json) {
		var category = new Category();
		update(category, json);
		return category;
	}

	@Override
	public void update(Category category, JsonObject json) {
		Util.mapBase(category, json, resolver);
		category.modelType = Json.getEnum(json, "modelType", ModelType.class);
	}
}
