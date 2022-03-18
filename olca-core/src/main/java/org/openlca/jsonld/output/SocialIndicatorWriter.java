package org.openlca.jsonld.output;

import org.openlca.core.model.SocialIndicator;

import com.google.gson.JsonObject;
import org.openlca.jsonld.Json;

record SocialIndicatorWriter(JsonExport exp) implements Writer<SocialIndicator> {

	@Override
	public JsonObject write(SocialIndicator i) {
		var obj = Writer.init(i);
		Json.put(obj, "activityVariable", i.activityVariable);
		Json.put(obj, "unitOfMeasurement", i.unitOfMeasurement);
		Json.put(obj, "evaluationScheme", i.evaluationScheme);
		Json.put(obj, "activityQuantity", exp.handleRef(i.activityQuantity));
		mapActivityUnit(i, obj);
		return obj;
	}

	private void mapActivityUnit(SocialIndicator i, JsonObject obj) {
		var unit = i.activityUnit == null && i.activityQuantity != null
			? i.activityQuantity.getReferenceUnit()
			: i.activityUnit;
		if (unit == null)
			return;
		Json.put(obj, "activityUnit", Json.asRef(unit));
	}

}
