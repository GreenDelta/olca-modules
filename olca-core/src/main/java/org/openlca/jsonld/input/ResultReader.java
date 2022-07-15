package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ImpactResult;
import org.openlca.core.model.Location;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record ResultReader(EntityResolver resolver)
	implements EntityReader<Result> {

	public ResultReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public Result read(JsonObject json) {
		var result = new Result();
		update(result, json);
		return result;
	}

	@Override
	public void update(Result result, JsonObject json) {
		Util.mapBase(result, json, resolver);
		var systemId = Json.getRefId(json, "productSystem");
		result.productSystem = resolver.get(ProductSystem.class, systemId);
		var methodId = Json.getRefId(json, "impactMethod");
		result.impactMethod = resolver.get(ImpactMethod.class, methodId);
		mapImpactResuts(json, result);
		mapFlowResults(json, result);
	}

	private void mapImpactResuts(JsonObject json, Result result) {
		result.impactResults.clear();
		var array = Json.getArray(json, "impactResults");
		if (array == null)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var r = new ImpactResult();
			var impactId = Json.getRefId(obj, "indicator");
			r.indicator = resolver.get(ImpactCategory.class, impactId);
			r.amount = Json.getDouble(obj, "amount", 0);
			r.description = Json.getString(obj, "description");
			result.impactResults.add(r);
		}
	}

	private void mapFlowResults(JsonObject json, Result result) {
		result.flowResults.clear();
		var array = Json.getArray(json, "flowResults");
		if (array == null)
			return;
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var obj = e.getAsJsonObject();
			var r = new FlowResult();
			result.flowResults.add(r);
			r.amount = Json.getDouble(obj, "amount", 0);
			r.isInput = Json.getBool(obj, "isInput", false);
			boolean isRef = Json.getBool(obj, "isRefFlow", false);
			if (isRef) {
				result.referenceFlow = r;
			}

			// location
			var locationId = Json.getRefId(obj, "location");
			r.location = resolver.get(Location.class, locationId);

			// flow and unit
			var flowId = Json.getRefId(obj, "flow");
			if (Strings.nullOrEmpty(flowId))
				continue;
			r.flow = resolver.get(Flow.class, flowId);
			var quantity = Quantity.of(r.flow, obj);
			r.flowPropertyFactor = quantity.factor();
			r.unit = quantity.unit();
		}
	}

}
