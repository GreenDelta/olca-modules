package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record ImpactCategoryReader(EntityResolver resolver)
	implements EntityReader<ImpactCategory> {

	public ImpactCategoryReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public ImpactCategory read(JsonObject json) {
		var impact = new ImpactCategory();
		update(impact, json);
		return impact;
	}

	@Override
	public void update(ImpactCategory impact, JsonObject json) {
		Util.mapBase(impact, json, resolver);

		impact.referenceUnit = Json.getString(json, "refUnit");
		impact.code = Json.getString(json, "code");
		var sourceId = Json.getString(json, "source");
		if (Strings.notEmpty(sourceId)) {
			impact.source = resolver.get(Source.class, sourceId);
		}
		mapParameters(impact, json);

		JsonArray factors = Json.getArray(json, "impactFactors");
		if (factors != null) {
			for (var e : factors) {
				if (!e.isJsonObject())
					continue;
				var factor = mapFactor(e.getAsJsonObject());
				if (factor == null)
					continue;
				impact.impactFactors.add(factor);
			}
		}

	}

	private void mapParameters(ImpactCategory impact, JsonObject json) {
		var parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (var e : parameters) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			var parameter = new Parameter();
			ParameterReader.mapFields(parameter, o);
			impact.parameters.add(parameter);
		}
	}

	private ImpactFactor mapFactor(JsonObject json) {
		if (json == null)
			return null;

		ImpactFactor factor = new ImpactFactor();

		// flow
		var flowId = Json.getRefId(json, "flow");
		var flow = resolver.get(Flow.class, flowId);
		factor.flow = flow;
		if (flow == null) {
			return null;
		}
		var quantity = Quantity.of(flow, json);
		factor.unit = quantity.unit();
		factor.flowPropertyFactor = quantity.factor();

		// amount fields
		factor.value = Json.getDouble(json, "value", 0);
		factor.formula = Json.getString(json, "formula");
		JsonElement uncertainty = json.get("uncertainty");
		if (uncertainty != null && uncertainty.isJsonObject()) {
			factor.uncertainty = Uncertainties.read(
				uncertainty.getAsJsonObject());
		}

		// location
		var locID = Json.getRefId(json, "location");
		if (Strings.notEmpty(locID)) {
			factor.location = resolver.get(Location.class, locID);
		}

		return factor;
	}
}
