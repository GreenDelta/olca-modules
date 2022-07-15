package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Result;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

public record EpdReader(EntityResolver resolver)
	implements EntityReader<Epd> {

	public EpdReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Epd read(JsonObject json) {
		var epd = new Epd();
		update(epd, json);
		return epd;
	}

	@Override
	public void update(Epd epd, JsonObject json) {
		Util.mapBase(epd, json, resolver);
		epd.urn = Json.getString(json, "urn");
		epd.manufacturer = actor(json, "manufacturer");
		epd.verifier = actor(json, "verifier");
		epd.programOperator = actor(json, "programOperator");
		var pcrId = Json.getRefId(json, "pcr");
		epd.pcr = resolver.get(Source.class, pcrId);
		epd.product = product(json);
		mapModules(epd, json);
	}

	private Actor actor(JsonObject json, String field) {
		var refId = Json.getRefId(json, field);
		return resolver.get(Actor.class, refId);
	}

	private EpdProduct product(JsonObject json) {
		var obj = Json.getObject(json, "product");
		if (obj == null)
			return null;
		var flowId = Json.getRefId(obj, "flow");
		var flow = resolver.get(Flow.class, flowId);
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

	private void mapModules(Epd epd, JsonObject json) {
		epd.modules.clear();
		var array = Json.getArray(json, "modules");
		if (array == null)
			return;
		for (var elem : array) {
			if (!elem.isJsonObject())
				continue;
			var obj = elem.getAsJsonObject();
			var module = new EpdModule();
			module.name = Json.getString(obj, "name");
			module.multiplier = Json.getDouble(obj, "multiplier", 1.0);
			var resultId = Json.getRefId(obj, "result");
			module.result = resolver.get(Result.class, resultId);
			epd.modules.add(module);
		}
	}
}
