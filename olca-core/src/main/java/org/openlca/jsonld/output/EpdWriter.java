package org.openlca.jsonld.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.Epd;
import org.openlca.jsonld.Json;

record EpdWriter(JsonExport exp) implements Writer<Epd> {

	@Override
	public JsonObject write(Epd epd) {
		var json = Writer.init(epd);

		Json.put(json, "urn", epd.urn);
		Json.put(json, "manufacturer", exp.handleRef(epd.manufacturer));
		Json.put(json, "verifier", exp.handleRef(epd.verifier));
		Json.put(json, "programOperator", exp.handleRef(epd.programOperator));
		Json.put(json, "pcr", exp.handleRef(epd.pcr));

		if (epd.product != null) {
			var productJson = new JsonObject();
			Json.put(productJson, "flow", exp.handleRef(epd.product.flow));
			Json.put(productJson, "flowProperty", exp.handleRef(epd.product.property));
			Json.put(productJson, "unit", Json.asRef(epd.product.unit));
			Json.put(productJson, "amount", epd.product.amount);
			json.add("product", productJson);
		}

		if (!epd.modules.isEmpty()) {
			var modsJson = new JsonArray();
			for (var mod : epd.modules) {
				var modObj = new JsonObject();
				Json.put(modObj, "name", mod.name);
				Json.put(modObj, "multiplier", mod.multiplier);
				Json.put(modObj, "result", exp.handleRef(mod.result));
				modsJson.add(modObj);
			}
			json.add("modules", modsJson);
		}

		return json;
	}
}
