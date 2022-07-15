package org.openlca.jsonld.input;

import java.util.Objects;

import com.google.gson.JsonObject;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.SocialIndicator;
import org.openlca.core.model.Unit;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

public record SocialIndicatorReader(EntityResolver resolver)
	implements EntityReader<SocialIndicator> {

	public SocialIndicatorReader(EntityResolver resolver) {
		this.resolver = Objects.requireNonNull(resolver);
	}

	@Override
	public SocialIndicator read(JsonObject json) {
		var indicator = new SocialIndicator();
		update(indicator, json);
		return indicator;
	}

	@Override
	public void update(SocialIndicator indicator, JsonObject json) {
		Util.mapBase(indicator, json, resolver);
		indicator.activityVariable = Json.getString(json, "activityVariable");
		indicator.evaluationScheme = Json.getString(json, "evaluationScheme");
		indicator.unitOfMeasurement = Json.getString(json, "unitOfMeasurement");

		// activity quantity and unit
		var quantityId = Json.getRefId(json, "activityQuantity");
		if (quantityId == null) {
			indicator.activityQuantity = null;
			indicator.activityUnit = null;
			return;
		}
		indicator.activityQuantity = resolver.get(FlowProperty.class, quantityId);
		indicator.activityUnit = findUnit(
			indicator.activityQuantity, Json.getRefId(json, "activityUnit"));
	}

	private Unit findUnit(FlowProperty prop, String unitId) {
		if (prop == null || prop.unitGroup == null || unitId == null)
			return null;
		for (var unit : prop.unitGroup.units) {
			if (Strings.nullOrEqual(unit.refId, unitId))
				return unit;
		}
		return null;
	}
}
