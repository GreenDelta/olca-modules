package org.openlca.jsonld.upgrades;

import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;
import org.openlca.jsonld.JsonStoreReader;

import com.google.gson.JsonObject;

class Upgrade4 extends Upgrade {

	Upgrade4(JsonStoreReader reader) {
		super(reader);
	}

	@Override
	public JsonObject get(ModelType type, String refId) {
		var obj = super.get(type, refId);
		if (obj == null)
			return null;
		if (type == ModelType.EPD) {
			var urn = Json.getString(obj, "urn");
			if (urn != null) {
				Json.put(obj, "registrationId", urn);
			}
		}
		return obj;
	}
}
