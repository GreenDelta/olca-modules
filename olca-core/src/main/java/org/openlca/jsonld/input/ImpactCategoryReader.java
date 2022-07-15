package org.openlca.jsonld.input;

import java.util.Objects;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Direction;
import org.openlca.core.model.Flow;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.Source;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

import com.google.gson.JsonObject;

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
		impact.direction = Json.getEnum(json, "direction", Direction.class);
		impact.code = Json.getString(json, "code");
		var sourceId = Json.getString(json, "source");
		if (Strings.notEmpty(sourceId)) {
			impact.source = resolver.get(Source.class, sourceId);
		}
		mapParameters(impact, json);

		// impact factors
		impact.impactFactors.clear();
		var factors = Json.getArray(json, "impactFactors");
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
		impact.parameters.clear();
		var parameters = Json.getArray(json, "parameters");
		if (parameters == null || parameters.size() == 0)
			return;
		for (var e : parameters) {
			if (!e.isJsonObject())
				continue;
			var o = e.getAsJsonObject();
			var parameter = new Parameter();
			ParameterReader.mapFields(parameter, o, resolver);
			parameter.scope = ParameterScope.IMPACT;
			impact.parameters.add(parameter);
		}
	}

	private ImpactFactor mapFactor(JsonObject json) {
		if (json == null)
			return null;

		var factor = new ImpactFactor();

		// flow
		var flowId = Json.getRefId(json, "flow");
		if (flowId == null)
			return null;
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
		var uncertainty = Json.getObject(json, "uncertainty");
		if (uncertainty != null ) {
			factor.uncertainty = Uncertainties.read(uncertainty);
		}

		// location
		var locId = Json.getRefId(json, "location");
		if (Strings.notEmpty(locId)) {
			factor.location = resolver.get(Location.class, locId);
		}

		return factor;
	}
}
