package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.ImpactCategory;

public record ImpactCategoryReader(EntityResolver resolver)
	implements EntityReader<ImpactCategory> {

	public ImpactCategoryReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public ImpactCategory read(JsonObject json) {
		var impact = new ImpactCategory();
		update(impact, json);
		return impact;
	}

	@Override
	public void update(ImpactCategory impact, JsonObject json) {
		Util.mapBase(impact, json, resolver);
	}
}
