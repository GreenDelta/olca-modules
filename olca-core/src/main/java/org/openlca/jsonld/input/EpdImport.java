package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.ModelType;
import org.openlca.jsonld.Json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class EpdImport extends BaseImport<Epd> {

	private EpdImport(String refId, JsonImport conf) {
		super(ModelType.EPD, refId, conf);
	}

	static Epd run(String refId, JsonImport conf) {
		return new EpdImport(refId, conf).run();
	}

	@Override
	Epd map(JsonObject json, long id) {
		if (json == null)
			return null;
		var epd = new Epd();
		In.mapAtts(json, epd, id, conf);
		epd.urn = Json.getString(json, "urn");
		epd.manufacturer = actor(json, "manufacturer");
		epd.verifier = actor(json, "verifier");
		epd.programOperator = actor(json, "programOperator");
		var pcrId = Json.getRefId(json, "pcr");
		epd.pcr = pcrId != null
			? SourceImport.run(pcrId, conf)
			: null;
		epd.product = product(json);
		epd.modules.addAll(modules(json));
		return conf.db.put(epd);
	}

	private EpdProduct product(JsonObject json) {
		var obj = Json.getObject(json, "product");
		if (obj == null)
			return null;
		var flowId = Json.getRefId(obj, "flow");
		var flow = flowId != null
			? FlowImport.run(flowId, conf)
			: null;
		if (flow == null)
			return null;
		var quantity = Quantity.of(flow, obj);
		var product = new EpdProduct();
		product.flow = flow;
		product.property = quantity.property();
		product.unit = quantity.unit();
		product.amount = Json.getDouble(obj, "amount", 0);
		return product;
	}

	private Actor actor(JsonObject json, String field) {
		var refId = Json.getRefId(json, field);
		return refId != null
			? ActorImport.run(refId, conf)
			: null;
	}

	private List<EpdModule> modules(JsonObject json) {
		var array = Json.getArray(json, "modules");
		if (array == null)
			return Collections.emptyList();
		var list = new ArrayList<EpdModule>();
		for (var elem : array) {
			if (!elem.isJsonObject())
				continue;
			var obj = elem.getAsJsonObject();
			var module = new EpdModule();
			module.name = Json.getString(obj, "name");
			module.multiplier = Json.getDouble(obj, "multiplier", 1.0);
			var resultId = Json.getRefId(obj, "result");
			module.result = resultId != null
				? ResultImport.run(resultId, conf)
				: null;
			list.add(module);
		}
		return list;
	}
}
