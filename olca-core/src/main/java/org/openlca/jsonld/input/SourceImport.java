package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class SourceImport extends BaseImport<Source> {

	private SourceImport(String refId, JsonImport conf) {
		super(ModelType.SOURCE, refId, conf);
	}

	static Source run(String refId, JsonImport conf) {
		return new SourceImport(refId, conf).run();
	}

	@Override
	Source map(JsonObject json, long id) {
		if (json == null)
			return null;
		Source s = new Source();
		In.mapAtts(json, s, id, conf);
		mapAtts(json, s);
		return conf.db.put(s);
	}

	private void mapAtts(JsonObject json, Source s) {
		s.url = Json.getString(json, "url");
		s.externalFile = Json.getString(json, "externalFile");
		s.textReference = Json.getString(json, "textReference");
		JsonElement year = json.get("year");
		if (year != null && year.isJsonPrimitive())
			s.year = year.getAsShort();
	}
}
