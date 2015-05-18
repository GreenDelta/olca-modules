package org.openlca.jsonld.input;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SourceImport {

	private Logger log = LoggerFactory.getLogger(getClass());
	private String refId;
	private ImportConfig conf;

	private SourceImport(String refId, ImportConfig conf) {
		this.refId = refId;
		this.conf = conf;
	}

	static Source run(String refId, ImportConfig conf) {
		return new SourceImport(refId, conf).run();
	}

	private Source run() {
		if (refId == null || conf == null)
			return null;
		try {
			Source s = conf.db.getSource(refId);
			if (s != null)
				return s;
			JsonObject json = conf.store.get(ModelType.SOURCE, refId);
			return map(json);
		} catch (Exception e) {
			log.error("failed to import source " + refId, e);
			return null;
		}
	}

	private Source map(JsonObject json) {
		if (json == null)
			return null;
		Source s = new Source();
		In.mapAtts(json, s);
		String catId = In.getRefId(json, "category");
		s.setCategory(CategoryImport.run(catId, conf));
		mapAtts(json, s);
		return conf.db.put(s);
	}

	private void mapAtts(JsonObject json, Source s) {
		s.setDoi(In.getString(json, "doi"));
		s.setExternalFile(In.getString(json, "externalFile"));
		s.setTextReference(In.getString(json, "textReference"));
		JsonElement year = json.get("year");
		if (year != null && year.isJsonPrimitive())
			s.setYear(year.getAsShort());
	}
}
