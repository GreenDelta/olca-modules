package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

public record SourceReader(EntityResolver resolver)
	implements EntityReader<Source> {

	public SourceReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Source read(JsonObject json) {
		var source = new Source();
		update(source, json);
		return source;
	}

	@Override
	public void update(Source source, JsonObject json) {
		Util.mapBase(source, json, resolver);
		source.url = Json.getString(json, "url");
		source.externalFile = Json.getString(json, "externalFile");
		source.textReference = Json.getString(json, "textReference");
		var year = json.get("year");
		if (year != null && year.isJsonPrimitive()) {
			source.year = year.getAsShort();
		}
	}
}
