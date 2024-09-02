package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.EpdModule;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.Flow;
import org.openlca.core.model.Location;
import org.openlca.core.model.Result;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;

import com.google.gson.JsonObject;

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
		epd.manufacturer = get(Actor.class, json, "manufacturer");
		epd.verifier = get(Actor.class, json, "verifier");
		epd.programOperator = get(Actor.class, json, "programOperator");
		epd.pcr = get(Source.class, json, "pcr");

		epd.validFrom = Json.getDate(json, "validFrom");
		epd.validUntil = Json.getDate(json, "validUntil");
		epd.location = get(Location.class, json, "location");
		epd.originalEpd = get(Source.class, json, "originalEpd");
		epd.manufacturing = Json.getString(json, "manufacturing");
		epd.productUsage = Json.getString(json, "productUsage");
		epd.useAdvice = Json.getString(json, "useAdvice");
		epd.registrationId = Json.getString(json, "registrationId");
		epd.dataGenerator = get(Actor.class, json, "dataGenerator");

		epd.product = product(json);
		mapModules(epd, json);
	}

	private <T extends RootEntity> T get(
			Class<T> type, JsonObject json, String field
	) {
		var refId = Json.getRefId(json, field);
		return refId != null
				? resolver.get(type, refId)
				: null;
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
