package org.openlca.jsonld.output;

import java.util.Objects;

import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class UnitGroupWriter extends Writer<UnitGroup> {

	UnitGroupWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	JsonObject write(UnitGroup ug) {
		JsonObject obj = super.write(ug);
		if (obj == null)
			return null;
		Out.put(obj, "defaultFlowProperty", ug.defaultFlowProperty, conf);
		mapUnits(ug, obj);
		return obj;
	}

	private void mapUnits(UnitGroup group, JsonObject json) {
		var units = new JsonArray();
		for (var unit : group.units) {
			var obj = new JsonObject();
			UnitWriter.map(unit, obj);
			if (Objects.equals(unit, group.referenceUnit)) {
				Out.put(obj, "referenceUnit", true);
			}
			units.add(obj);
		}
		Out.put(json, "units", units);
	}

}
