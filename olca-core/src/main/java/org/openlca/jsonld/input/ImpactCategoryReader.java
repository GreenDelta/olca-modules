package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.Location;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.Source;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
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

		impact.referenceUnit = Json.getString(json, "referenceUnitName");
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

		// set the flow property and unit; if we cannot find them
		// we will choose the reference data from the flow
		// when we cannot find consistent information we return
		// a factor where the unit or flow property factor may
		// is absent.
		Unit unit = conf.db.get(ModelType.UNIT, Json.getRefId(json, "unit"));
		FlowPropertyFactor propFac = getPropertyFactor(json, flow);
		if (unit != null && propFac != null) {
			factor.unit = unit;
			factor.flowPropertyFactor = propFac;
			return factor;
		}

		if (propFac == null) {
			propFac = flow.getReferenceFactor();
			if (propFac == null || propFac.flowProperty == null)
				return factor;
		}
		factor.flowPropertyFactor = propFac;

		UnitGroup ug = propFac.flowProperty.unitGroup;
		if (ug == null)
			return factor;

		if (unit == null) {
			factor.unit = ug.referenceUnit;
		} else {
			for (Unit u : ug.units) {
				if (Objects.equals(u, unit)) {
					factor.unit = u;
				}
			}
		}
		return factor;
	}

	private FlowPropertyFactor getPropertyFactor(JsonObject json,
		Flow flow) {
		if (json == null || flow == null)
			return null;
		String propId = Json.getRefId(json, "flowProperty");
		for (FlowPropertyFactor fac : flow.flowPropertyFactors) {
			FlowProperty prop = fac.flowProperty;
			if (prop == null)
				continue;
			if (Objects.equals(propId, prop.refId))
				return fac;
		}
		return null;
	}
}
