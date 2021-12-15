package org.openlca.io.maps;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Strings;

public record SyncFlow(
	Flow flow,
	FlowPropertyFactor property,
	Unit unit,
	ProcessDescriptor provider,
	boolean isMapped,
	double mapFactor) {

	private static final SyncFlow empty = new SyncFlow(
		null, null, null, null, false, 0);

	public static SyncFlow of(Flow flow) {
		return flow != null
			? new SyncFlow(flow, null, null, null, false, 1.0)
			: empty;
	}

	public static SyncFlow empty() {
		return empty;
	}

	public boolean isEmpty() {
		return flow == null;
	}

	public boolean isPresent() {
		return flow != null;
	}

	public static SyncFlow mapped(FlowMapEntry entry, IDatabase db) {
		if (entry == null || entry.targetFlow() == null)
			return empty;
		var ref = entry.targetFlow();

		if (ref.flow == null)
			return empty;
		var flow = db.get(Flow.class, ref.flow.refId);
		if (flow == null)
			return empty;

		// check for a provider
		ProcessDescriptor provider = null;
		if (ref.provider != null
			&& flow.flowType != FlowType.ELEMENTARY_FLOW) {
			provider = new ProcessDao(db)
				.getDescriptorForRefId(ref.provider.refId);
		}

		// check the flow property; default is the reference
		// flow property of the flow
		FlowPropertyFactor property = null;
		var propRef = ref.property;
		if (propRef != null && (
			Strings.notEmpty(propRef.refId) || Strings.notEmpty(propRef.name))) {

			// search for a matching name or ID
			for (var p : flow.flowPropertyFactors) {
				if (p.flowProperty == null)
					continue;
				var other = p.flowProperty;
				if (other.refId != null && other.refId.equals(propRef.refId)) {
					property = p;
					break;
				}
				if (other.name != null && other.name.equalsIgnoreCase(propRef.name)) {
					property = p; // no break here; further search for a matching ID
				}
			}

			// if a flow property is defined,
			// it must be available in the flow
			if (property == null)
				return empty;
		}

		// check the unit
		Unit unit = null;
		var unitRef = ref.unit;
		if (unitRef != null && (
			Strings.notEmpty(unitRef.refId) || Strings.notEmpty(unitRef.name))) {

			// if a unit is defined, a corresponding unit group must exist
			var prop = property != null
				? property
				: flow.getReferenceFactor();
			if (prop == null
				|| prop.flowProperty == null
				|| prop.flowProperty.unitGroup == null)
				return empty;

			for (var u : prop.flowProperty.unitGroup.units) {
				if (u.refId != null && u.refId.equals(unitRef.refId)) {
					unit = u;
					break;
				}
				// do not ignore the case for units!
				if (u.name != null && u.name.equals(unitRef.name)) {
					unit = u;
				}
			}

			// if a unit is defined, it must be
			// available in the unit group
			if (unit == null)
				return empty;
		}

		return new SyncFlow(flow, property, unit, provider, true, entry.factor());
	}

	@Override
	public FlowPropertyFactor property() {
		return property == null
			? flow.getReferenceFactor()
			: property;
	}

	@Override
	public Unit unit() {
		if (unit != null)
			return unit;
		var property = property();
		return property != null && property.flowProperty != null
			? property.flowProperty.getReferenceUnit()
			: null;
	}

}
