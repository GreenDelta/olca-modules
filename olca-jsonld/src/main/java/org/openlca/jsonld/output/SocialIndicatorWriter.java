package org.openlca.jsonld.output;

import java.util.function.Consumer;

import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.core.model.UnitGroup;

import com.google.gson.JsonObject;

class SocialIndicatorWriter extends Writer<SocialIndicator> {

	@Override
	public JsonObject write(SocialIndicator i, Consumer<RootEntity> refFn) {
		JsonObject obj = super.write(i, refFn);
		if (obj == null)
			return null;
		Out.put(obj, "activityVariable", i.activityVariable);
		Out.put(obj, "activityQuantity", i.activityQuantity, refFn);
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
		Out.put(obj, "activityUnit", unit, null);
	}

}
