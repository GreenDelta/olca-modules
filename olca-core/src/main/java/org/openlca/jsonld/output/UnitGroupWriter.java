package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record UnitGroupWriter(JsonExport exp) implements Writer<UnitGroup> {

	@Override
	public JsonObject write(UnitGroup ug) {
		var obj = Writer.init(ug);
		Json.put(obj, "defaultFlowProperty", exp.handleRef(ug.defaultFlowProperty));
		mapUnits(ug, obj);
		return obj;
	}

	private void mapUnits(UnitGroup group, JsonObject json) {
		var units = new JsonArray();
		for (var unit : group.units) {
			var obj = new JsonObject();
			UnitWriter.map(unit, obj);
			if (Objects.equals(unit, group.referenceUnit)) {
				Json.put(obj, "isRefUnit", true);
			}
			units.add(obj);
		}
		Json.put(json, "units", units);
	}

}
