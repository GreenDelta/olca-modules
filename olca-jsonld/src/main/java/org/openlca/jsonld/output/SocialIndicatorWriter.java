package org.openlca.jsonld.output;

import java.lang.reflect.Type;

import org.openlca.core.model.ModelType;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.UnitGroup;
import org.openlca.jsonld.EntityStore;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

class SocialIndicatorWriter implements Writer<SocialIndicator> {

	private EntityStore store;

	public SocialIndicatorWriter() {
	}

	public SocialIndicatorWriter(EntityStore store) {
		this.store = store;
	}

	@Override
	public void write(SocialIndicator i) {
		if (i == null || store == null)
			return;
		if (store.contains(ModelType.SOCIAL_INDICATOR, i.getRefId()))
			return;
		JsonObject obj = serialize(i, null, null);
		store.put(ModelType.SOCIAL_INDICATOR, obj);
	}

	@Override
	public JsonObject serialize(SocialIndicator i, Type t,
			JsonSerializationContext c) {
		JsonObject obj = store == null ? new JsonObject() : store.initJson();
		map(i, obj);
		return obj;
	}

	private void map(SocialIndicator i, JsonObject obj) {
		if (i == null || obj == null)
			return;
		Out.addAttributes(i, obj, store);
		obj.addProperty("activityVariable", i.activityVariable);
		JsonObject qObj = Out.put(i.activityQuantity, store);
		obj.add("activityQuantity", qObj);
		if (i.activityQuantity != null) {
			UnitGroup ug = i.activityQuantity.getUnitGroup();
			if (ug != null) {
				Out.put(ug, store);
				JsonObject uObj = Out.createRef(i.activityUnit);
				obj.add("activityUnit", uObj);
			}
		}
		obj.addProperty("unitOfMeasurement", i.unitOfMeasurement);
		obj.addProperty("evaluationScheme", i.evaluationScheme);
	}
}
