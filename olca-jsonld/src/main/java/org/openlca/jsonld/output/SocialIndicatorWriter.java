package org.openlca.jsonld.output;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonObject;

class SocialIndicatorWriter extends Writer<SocialIndicator> {

	SocialIndicatorWriter(ExportConfig conf) {
		super(conf);
	}

	@Override
	public JsonObject write(SocialIndicator i) {
		JsonObject obj = super.write(i);
		if (obj == null)
			return null;
		Out.put(obj, "activityVariable", i.activityVariable);
		Out.put(obj, "activityQuantity", i.activityQuantity, conf);
		Out.put(obj, "unitOfMeasurement", i.unitOfMeasurement);
		Out.put(obj, "evaluationScheme", i.evaluationScheme);
		mapActivityUnit(i, obj);
		return obj;
	}

	private void mapActivityUnit(SocialIndicator i, JsonObject obj) {
		FlowProperty quantity = i.activityQuantity;
		if (quantity == null || quantity.getUnitGroup() == null)
			return;
		UnitGroup group = quantity.getUnitGroup();
		if (group == null || group.getReferenceUnit() == null)
			return;
		Unit unit = group.getReferenceUnit();
		Out.put(obj, "activityUnit", unit, conf);
	}

}
