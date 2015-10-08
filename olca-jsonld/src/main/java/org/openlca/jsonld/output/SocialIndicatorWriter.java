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
		obj.addProperty("activityVariable", i.activityVariable);
		JsonObject qObj = createRef(i.activityQuantity, refFn);
		obj.add("activityQuantity", qObj);
		addActivityUnit(i, obj);
		obj.addProperty("unitOfMeasurement", i.unitOfMeasurement);
		obj.addProperty("evaluationScheme", i.evaluationScheme);
		return obj;
	}

	private void addActivityUnit(SocialIndicator i, JsonObject obj) {
		FlowProperty quantity = i.activityQuantity;
		if (quantity == null || quantity.getUnitGroup() == null)
			return;
		UnitGroup group = quantity.getUnitGroup();
		if (group == null || group.getReferenceUnit() == null)
			return;
		Unit unit = group.getReferenceUnit();
		JsonObject uObj = new JsonObject();
		uObj.addProperty("@type", "Unit");
		uObj.addProperty("@id", unit.getRefId());
		uObj.addProperty("name", unit.getName());
		obj.add("activityUnit", uObj);
	}

}
