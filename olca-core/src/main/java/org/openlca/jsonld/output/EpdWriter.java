package org.openlca.jsonld.output;

import org.openlca.core.model.Epd;
import org.openlca.jsonld.Json;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public record EpdWriter(JsonExport exp) implements JsonWriter<Epd> {

	@Override
	public JsonObject write(Epd epd) {
		var json = Util.init(epd);

		Json.put(json, "urn", epd.urn);
		Json.put(json, "manufacturer", exp.handleRef(epd.manufacturer));
		Json.put(json, "verifier", exp.handleRef(epd.verifier));
		Json.put(json, "programOperator", exp.handleRef(epd.programOperator));
		Json.put(json, "pcr", exp.handleRef(epd.pcr));

		Json.put(json, "epdType", epd.epdType);
		Json.put(json, "validFrom", Json.asDate(epd.validFrom));
		Json.put(json, "validUntil", Json.asDate(epd.validUntil));
		Json.put(json, "location", exp.handleRef(epd.location));
		Json.put(json, "originalEpd", exp.handleRef(epd.originalEpd));
		Json.put(json, "manufacturing", epd.manufacturing);
		Json.put(json, "productUsage", epd.productUsage);
		Json.put(json, "useAdvice", epd.useAdvice);
		Json.put(json, "registrationId", epd.registrationId);
		Json.put(json, "dataGenerator", exp.handleRef(epd.dataGenerator));

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
