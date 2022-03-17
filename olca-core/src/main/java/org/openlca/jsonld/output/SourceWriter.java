package org.openlca.jsonld.output;

import org.openlca.core.model.Source;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class SourceWriter extends Writer<Source> {

	SourceWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	public JsonObject write(Source source) {
		JsonObject obj = super.write(source);
		if (obj == null)
			return null;
		Json.put(obj, "url", source.url);
		Json.put(obj, "externalFile", source.externalFile);
		Json.put(obj, "textReference", source.textReference);
		Json.put(obj, "year", source.year);
		return obj;
	}

}
