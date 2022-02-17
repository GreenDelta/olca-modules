package org.openlca.jsonld.output;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.core.model.Epd;

class EpdWriter extends Writer<Epd> {

	EpdWriter(ExportConfig config) {
		super(config);
	}

	@Override
	JsonObject write(Epd epd) {
		var json = super.write(epd);
		if (json == null)
			return null;

		Out.put(json, "urn", epd.urn);
		Out.put(json, "manufacturer", epd.manufacturer, conf);
		Out.put(json, "verifier", epd.verifier, conf);
		Out.put(json, "programOperator", epd.programOperator, conf);
		Out.put(json, "pcr", epd.pcr, conf);

		if (epd.product != null) {
			var productJson = new JsonObject();
			Out.put(productJson, "flow", epd.product.flow, conf);
			Out.put(productJson, "flowProperty", epd.product.property, conf);
			Out.put(productJson, "unit", epd.product.unit, conf);
			Out.put(productJson, "amount", epd.product.amount);
			json.add("product", productJson);
		}

		if (!epd.modules.isEmpty()) {
			var modsJson = new JsonArray();
			for (var mod : epd.modules) {
				var modObj = new JsonObject();
				Out.put(modObj, "name", mod.name);
				Out.put(modObj, "result", mod.result, conf);
				modsJson.add(modObj);
			}
			json.add("modules", modsJson);
		}

		return json;
	}
}
