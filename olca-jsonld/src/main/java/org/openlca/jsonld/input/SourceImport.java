package org.openlca.jsonld.input;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

class SourceImport extends BaseImport<Source> {

	private SourceImport(String refId, ImportConfig conf) {
		super(ModelType.SOURCE, refId, conf);
	}

	static Source run(String refId, ImportConfig conf) {
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
		s.setUrl(In.getString(json, "url"));
		s.setExternalFile(In.getString(json, "externalFile"));
		s.setTextReference(In.getString(json, "textReference"));
		JsonElement year = json.get("year");
		if (year != null && year.isJsonPrimitive())
			s.setYear(year.getAsShort());
	}
}
