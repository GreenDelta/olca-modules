package org.openlca.jsonld.input;

import com.google.gson.JsonObject;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.Unit;
import org.openlca.jsonld.Json;
import org.openlca.util.Strings;

/**
 * There are multiple places where the flow property and unit of a quantity
 * of a flow is stored in a Json object (e.g. in exchanges, characterization
 * factors, or result values). This utility class holds the respective entries
 * of such an object.
 */
record Quantity(Flow flow, FlowPropertyFactor factor, Unit unit) {

	static Quantity of(Flow flow, JsonObject obj) {
		return of(flow, obj, "flowProperty", "unit");
	}

	static Quantity of(
		Flow flow, JsonObject obj, String propertyField, String unitField) {
		if (flow == null)
			return new Quantity(null, null, null);
		if (obj == null)
			return new Quantity(
				flow, flow.getReferenceFactor(), flow.getReferenceUnit());
		var factor = propertyOf(flow, obj, propertyField);
		var unit = unitOf(flow, factor, obj, unitField);
		return new Quantity(flow, factor, unit);
	}

	private static FlowPropertyFactor propertyOf(
		Flow flow, JsonObject obj, String field) {
		var propId = Json.getRefId(obj, field);
		if (Strings.nullOrEmpty(propId))
			return flow.getReferenceFactor();
		for (var factor : flow.flowPropertyFactors) {
			var prop = factor.flowProperty;
			if (prop != null && propId.equals(prop.refId))
				return factor;
		}
		return null;
	}

	private static Unit unitOf(
		Flow flow, FlowPropertyFactor factor, JsonObject obj, String field) {
		var property = factor != null
			? factor.flowProperty
			: flow.referenceFlowProperty;
		if (property == null || property.unitGroup == null)
			return null;
		var unitId = Json.getRefId(obj, field);
		if (Strings.nullOrEmpty(unitId))
			return property.getReferenceUnit();
		for (var unit : property.unitGroup.units) {
			if (unitId.equals(unit.refId))
				return unit;
		}
		return null;
	}

	public FlowProperty property() {
		return factor != null ? factor.flowProperty : null;
	}
}
