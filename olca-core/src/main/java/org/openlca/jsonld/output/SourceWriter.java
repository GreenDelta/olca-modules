package org.openlca.jsonld.output;

import org.openlca.core.model.Source;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

public record SourceWriter(JsonExport exp) implements JsonWriter<Source> {

	@Override
	public JsonObject write(Source source) {
		var obj = Util.init(source);
		Json.put(obj, "url", source.url);
		Json.put(obj, "externalFile", source.externalFile);
		Json.put(obj, "textReference", source.textReference);
		Json.put(obj, "year", source.year);
		return obj;
	}

}
