package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

class UnitGroupWriter extends Writer<UnitGroup> {

	UnitGroupWriter(JsonExport exp) {
		super(exp);
	}

	@Override
	JsonObject write(UnitGroup ug) {
		var obj = super.write(ug);
		if (obj == null)
			return null;
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
				Json.put(obj, "referenceUnit", true);
			}
			units.add(obj);
		}
		Json.put(json, "units", units);
	}

}
