package org.openlca.jsonld.input;

import java.util.HashMap;
import java.util.Objects;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.Json;

public record UnitGroupReader(EntityResolver resolver)
	implements EntityReader<UnitGroup> {

	public UnitGroupReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public UnitGroup read(JsonObject json) {
		var group = new UnitGroup();
		update(group, json);
		return group;
	}

	@Override
	public void update(UnitGroup group, JsonObject json) {
		Util.mapBase(group, json, resolver);
		var propId = Json.getRefId(json, "defaultFlowProperty");
		if (propId != null) {
			group.defaultFlowProperty = resolver.get(FlowProperty.class, propId);
		}
		mapUnits(group, json);
	}

	private void mapUnits(UnitGroup group, JsonObject json) {

		// sync. with existing units if we are in update mode
		var oldUnits = new HashMap<String, Unit>();
		for (var oldUnit : group.units) {
			oldUnits.put(oldUnit.name, oldUnit);
		}
		group.units.clear();

		var array = Json.getArray(json, "units");
		if (array == null || array.size() == 0)
			return;
		for (JsonElement e : array) {
			if (!e.isJsonObject())
				continue;
			var unitJson = e.getAsJsonObject();
			var name = Json.getString(unitJson, "name");
			if (name == null)
				continue;

			var unit = oldUnits.computeIfAbsent(
				name, n -> new Unit());

			// map unit attributes
			Util.mapBase(unit, unitJson, resolver);
			unit.conversionFactor = Json.getDouble(
				unitJson, "conversionFactor", 1.0);
			var synonyms = Json.getArray(unitJson, "synonyms");
			if (synonyms != null) {
				unit.synonyms = Json.stream(synonyms)
					.filter(JsonElement::isJsonPrimitive)
					.map(JsonElement::getAsString)
					.reduce((acc, syn) -> acc + ";" + syn)
					.orElse(null);
			}

			boolean refUnit = Json.getBool(unitJson, "isRefUnit", false);
			if (refUnit) {
				group.referenceUnit = unit;
			}
			group.units.add(unit);
		}
	}
}
