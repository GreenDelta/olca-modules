package org.openlca.jsonld.output;

import com.google.gson.JsonObject;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.jsonld.Json;

class Out {

	private Out() {
	}

	JsonObject checkExportRef(CategorizedEntity e, ExportConfig config) {
		if (e == null)
			return null;
		var ref = Json.asRef(e);
		if (config.exportReferences) {
			config.refFn.accept(e);
		}
	}




}
